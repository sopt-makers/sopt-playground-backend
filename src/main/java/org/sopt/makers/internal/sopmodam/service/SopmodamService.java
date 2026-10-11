package org.sopt.makers.internal.sopmodam.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.exception.BadRequestException;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.external.platform.PlatformService;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.service.MemberRetriever;
import org.sopt.makers.internal.sopmodam.domain.SopmodamQuestion;
import org.sopt.makers.internal.sopmodam.domain.SopmodamRound;
import org.sopt.makers.internal.sopmodam.domain.enums.SopmodamCardType;
import org.sopt.makers.internal.sopmodam.domain.enums.SopmodamMemberType;
import org.sopt.makers.internal.sopmodam.domain.enums.SopmodamPhase;
import org.sopt.makers.internal.sopmodam.dto.response.SopmodamAnswerResponse;
import org.sopt.makers.internal.sopmodam.dto.response.SopmodamCurrentCardResponse;
import org.sopt.makers.internal.sopmodam.dto.response.SopmodamMyAnswerResponse;
import org.sopt.makers.internal.sopmodam.dto.response.SopmodamQuestionListResponse;
import org.sopt.makers.internal.sopmodam.dto.response.SopmodamQuestionResponse;
import org.sopt.makers.internal.sopmodam.dto.response.SopmodamVoteResponse;
import org.sopt.makers.internal.sopmodam.util.DdayUtil;
import org.springframework.stereotype.Service;

// Platform 호출 동안 DB 커넥션을 잡지 않도록 서비스 메서드에는 트랜잭션을 걸지 않는다.
// 쓰기 트랜잭션은 SopmodamVoteModifier 가 잡는다.
@Service
@RequiredArgsConstructor
public class SopmodamService {

    private final SopmodamRoundRetriever roundRetriever;
    private final SopmodamQuestionRetriever questionRetriever;
    private final SopmodamVoteRetriever voteRetriever;
    private final SopmodamAnswerRetriever answerRetriever;
    private final SopmodamVoteModifier voteModifier;
    private final SopmodamMemberPolicy memberPolicy;
    private final MemberRetriever memberRetriever;
    private final PlatformService platformService;

    private final ZoneId KST = ZoneId.of("Asia/Seoul");

    // 보여줄 카드가 없으면 비어 있다. 현재 회차가 없거나, 카드를 보여주지 않는 사용자이거나, 최종 질문이 정해지지 않은 경우다
    public Optional<SopmodamCurrentCardResponse> getCurrentCard(Long userId) {
        LocalDateTime now = LocalDateTime.now(KST);
        Optional<SopmodamRound> currentRound = roundRetriever.findCurrentRound(now);
        if (currentRound.isEmpty()) {
            return Optional.empty();
        }
        SopmodamRound round = currentRound.get();
        SopmodamPhase phase = SopmodamPhase.of(round, now);

        InternalUserDetails user = platformService.getInternalUser(userId);
        SopmodamMemberType memberType = memberPolicy.resolveType(user, round.getGeneration());
        Optional<SopmodamCardType> cardType = SopmodamCardType.of(phase, memberType);
        if (cardType.isEmpty()) {
            return Optional.empty();
        }

        if (cardType.get() == SopmodamCardType.VOTE) {
            return Optional.of(toVoteCard(round, phase, userId, now));
        }

        // 투표가 끝난 뒤의 카드는 모두 최종 질문을 보여주므로, 최종 질문이 정해지지 않았으면 카드를 숨긴다
        Optional<SopmodamQuestion> finalQuestion = questionRetriever.findFinalQuestion(round.getId());
        if (finalQuestion.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(toAnswerCard(round, phase, cardType.get(), finalQuestion.get(), userId, now));
    }

    public SopmodamQuestionListResponse getQuestions(Long userId, Long roundId) {
        LocalDateTime now = LocalDateTime.now(KST);
        SopmodamRound round = roundRetriever.findRoundById(roundId);
        validateCanVote(userId, round);

        SopmodamPhase phase = SopmodamPhase.of(round, now);
        if (phase == SopmodamPhase.SCHEDULED) {
            throw new BadRequestException("투표 기간이 아닙니다.");
        }

        List<SopmodamQuestionResponse> questions = questionRetriever.findAllByRoundId(roundId).stream()
            .map(SopmodamQuestionResponse::from)
            .toList();
        Long myVoteQuestionId = voteRetriever.findVotedQuestionId(roundId, userId).orElse(null);

        boolean isVoting = phase == SopmodamPhase.VOTING;
        boolean hasVoted = myVoteQuestionId != null;

        return new SopmodamQuestionListResponse(
            round.getId(),
            round.getEventName(),
            round.getVoteEndAt(),
            isVoting ? DdayUtil.calculateDDay(round.getVoteEndAt(), now) : null,
            isVoting && !hasVoted,
            hasVoted,
            myVoteQuestionId,
            questions
        );
    }

    public SopmodamVoteResponse vote(Long userId, Long roundId, Long questionId) {
        LocalDateTime now = LocalDateTime.now(KST);
        SopmodamRound round = roundRetriever.findRoundById(roundId);
        validateCanVote(userId, round);

        if (SopmodamPhase.of(round, now) != SopmodamPhase.VOTING) {
            throw new BadRequestException("투표 기간이 아닙니다.");
        }

        SopmodamQuestion question = questionRetriever.findQuestionInRound(questionId, roundId);
        voteRetriever.validateNotVoted(roundId, userId);
        // 회원을 먼저 확인해 두면 투표 저장 시 무결성 위반은 회차당 1표 유니크 제약뿐이다
        Member member = memberRetriever.findMemberById(userId);

        voteModifier.createVote(round, question, member);
        return new SopmodamVoteResponse(roundId, questionId);
    }

    public SopmodamMyAnswerResponse getMyAnswer(Long userId, Long roundId) {
        LocalDateTime now = LocalDateTime.now(KST);
        SopmodamRound round = roundRetriever.findRoundById(roundId);
        validateCanAnswer(userId, round);

        // 투표 중에도 단독 1위 질문은 조회되므로, 투표가 끝나기 전에는 최종 질문이 없는 것으로 본다
        SopmodamPhase phase = SopmodamPhase.of(round, now);
        boolean isVoteEnded = phase == SopmodamPhase.ANSWERING || phase == SopmodamPhase.REVEALED;
        SopmodamQuestion finalQuestion = questionRetriever.findFinalQuestion(roundId)
            .filter(question -> isVoteEnded)
            .orElseThrow(() -> new BadRequestException("최종 질문이 아직 선정되지 않았습니다."));
        SopmodamAnswerResponse answer = answerRetriever.findMyAnswer(roundId, userId)
            .map(SopmodamAnswerResponse::from)
            .orElse(null);

        // 답변 자격은 위에서 검증했으므로 답변 모집 기간인지만 보면 된다
        boolean canEdit = phase == SopmodamPhase.ANSWERING;

        return new SopmodamMyAnswerResponse(
            round.getId(),
            SopmodamQuestionResponse.from(finalQuestion),
            round.getAnswerEndAt(),
            canEdit,
            answer
        );
    }

    private void validateCanVote(Long userId, SopmodamRound round) {
        InternalUserDetails user = platformService.getInternalUser(userId);
        SopmodamMemberType memberType = memberPolicy.resolveType(user, round.getGeneration());
        memberPolicy.validateCanVote(memberType);
    }

    private void validateCanAnswer(Long userId, SopmodamRound round) {
        InternalUserDetails user = platformService.getInternalUser(userId);
        SopmodamMemberType memberType = memberPolicy.resolveType(user, round.getGeneration());
        memberPolicy.validateCanAnswer(memberType);
    }

    private SopmodamCurrentCardResponse toVoteCard(SopmodamRound round, SopmodamPhase phase, Long userId, LocalDateTime now) {
        boolean hasVoted = voteRetriever.findVotedQuestionId(round.getId(), userId).isPresent();

        return new SopmodamCurrentCardResponse(
            round.getId(),
            round.getEventName(),
            phase,
            SopmodamCardType.VOTE,
            round.getVoteEndAt(),
            round.getAnswerEndAt(),
            DdayUtil.calculateDDay(round.getVoteEndAt(), now),
            null,
            !hasVoted,
            hasVoted,
            false,
            false,
            null
        );
    }

    private SopmodamCurrentCardResponse toAnswerCard(
        SopmodamRound round,
        SopmodamPhase phase,
        SopmodamCardType cardType,
        SopmodamQuestion finalQuestion,
        Long userId,
        LocalDateTime now
    ) {
        boolean isCollecting = cardType == SopmodamCardType.ANSWER_COLLECTING;
        boolean hasAnswered = isCollecting && answerRetriever.hasAnswered(round.getId(), userId);
        boolean isRevealed = cardType == SopmodamCardType.REVEALED;

        return new SopmodamCurrentCardResponse(
            round.getId(),
            round.getEventName(),
            phase,
            cardType,
            round.getVoteEndAt(),
            round.getAnswerEndAt(),
            isRevealed ? null : DdayUtil.calculateDDay(round.getAnswerEndAt(), now),
            SopmodamQuestionResponse.from(finalQuestion),
            false,
            false,
            isCollecting && !hasAnswered,
            hasAnswered,
            isRevealed ? answerRetriever.countByRoundId(round.getId()) : null
        );
    }
}
