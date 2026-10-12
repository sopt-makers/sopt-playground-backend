package org.sopt.makers.internal.sopmodam.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.exception.BadRequestException;
import org.sopt.makers.internal.exception.ForbiddenException;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.external.platform.PlatformService;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.service.MemberRetriever;
import org.sopt.makers.internal.sopmodam.domain.SopmodamAnswer;
import org.sopt.makers.internal.sopmodam.domain.SopmodamQuestion;
import org.sopt.makers.internal.sopmodam.domain.SopmodamRound;
import org.sopt.makers.internal.sopmodam.domain.enums.SopmodamCardType;
import org.sopt.makers.internal.sopmodam.domain.enums.SopmodamMemberType;
import org.sopt.makers.internal.sopmodam.domain.enums.SopmodamPhase;
import org.sopt.makers.internal.sopmodam.dto.response.SopmodamCurrentCardResponse;
import org.sopt.makers.internal.sopmodam.dto.request.SopmodamAnswerCreateRequest;
import org.sopt.makers.internal.sopmodam.dto.request.SopmodamAnswerUpdateRequest;
import org.sopt.makers.internal.sopmodam.dto.response.SopmodamAnswerCreateResponse;
import org.sopt.makers.internal.sopmodam.dto.response.SopmodamAnswerItemResponse;
import org.sopt.makers.internal.sopmodam.dto.response.SopmodamAnswerListResponse;
import org.sopt.makers.internal.sopmodam.dto.response.SopmodamAnswerUpdateResponse;
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
    private final SopmodamRoundModifier roundModifier;
    private final SopmodamQuestionRetriever questionRetriever;
    private final SopmodamVoteRetriever voteRetriever;
    private final SopmodamAnswerRetriever answerRetriever;
    private final SopmodamVoteModifier voteModifier;
    private final SopmodamAnswerModifier answerModifier;
    private final SopmodamMemberPolicy memberPolicy;
    private final SopmodamRoundPolicy roundPolicy;
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
        round = roundModifier.selectFinalQuestionIfAbsent(round, now);
        Optional<SopmodamQuestion> finalQuestion = questionRetriever.findFinalQuestion(round);
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

    public SopmodamAnswerListResponse getAnswers(Long roundId) {
        LocalDateTime now = LocalDateTime.now(KST);
        SopmodamRound round = roundRetriever.findRoundById(roundId);

        if (SopmodamPhase.of(round, now) != SopmodamPhase.REVEALED) {
            throw new BadRequestException("아직 공개되지 않은 답변입니다.");
        }

        round = roundModifier.selectFinalQuestionIfAbsent(round, now);
        SopmodamQuestion selectedQuestion = questionRetriever.findSelectedQuestionInRound(round);
        List<SopmodamAnswer> answers = answerRetriever.findAllByRoundId(roundId);

        List<Long> memberIds = answers.stream()
            .map(a -> a.getMember().getId())
            .toList();
        Map<Long, InternalUserDetails> userMap = platformService.getInternalUserDetailsMap(memberIds);

        List<SopmodamAnswerItemResponse> answerItems = answers.stream()
            .map(a -> SopmodamAnswerItemResponse.of(a, userMap.get(a.getMember().getId())))
            .toList();

        return new SopmodamAnswerListResponse(
            round.getId(),
            round.getEventName(),
            SopmodamQuestionResponse.from(selectedQuestion),
            answers.size(),
            answerItems
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

    public SopmodamAnswerCreateResponse createAnswer(Long userId, Long roundId, SopmodamAnswerCreateRequest request) {
        validateAnswerContent(request.content());

        LocalDateTime now = LocalDateTime.now(KST);
        SopmodamRound round = roundRetriever.findRoundById(roundId);

        InternalUserDetails user = platformService.getInternalUser(userId);
        SopmodamMemberType memberType = memberPolicy.resolveType(user, round.getGeneration());
        memberPolicy.validateCanAnswer(memberType);

        roundPolicy.validateAnsweringPhase(round, now);

        round = roundModifier.selectFinalQuestionIfAbsent(round, now);
        SopmodamQuestion selectedQuestion = questionRetriever.findSelectedQuestionInRound(round);
        answerRetriever.validateNotAnswered(selectedQuestion.getId(), userId);
        // 회원을 먼저 확인해 두면 답변 저장 시 무결성 위반은 유니크 제약뿐이다
        Member member = memberRetriever.findMemberById(userId);

        SopmodamAnswer answer = answerModifier.createAnswer(selectedQuestion, member, request.content());
        return SopmodamAnswerCreateResponse.from(answer);
    }

    public SopmodamAnswerUpdateResponse updateAnswer(Long userId, Long answerId, SopmodamAnswerUpdateRequest request) {
        validateAnswerContent(request.content());

        LocalDateTime now = LocalDateTime.now(KST);
        SopmodamAnswer answer = answerRetriever.findAnswerById(answerId);

        if (!answer.getMember().getId().equals(userId)) {
            throw new ForbiddenException("본인의 답변이 아닙니다.");
        }

        SopmodamRound round = answer.getQuestion().getRound();
        roundPolicy.validateAnsweringPhase(round, now);

        SopmodamAnswer updated = answerModifier.updateAnswer(answer, request.content());
        return SopmodamAnswerUpdateResponse.from(updated);
    }

    private void validateAnswerContent(String content) {
        if (content == null || content.length() > 500) {
            throw new BadRequestException("답변 글자수가 초과되었습니다.");
        }
        if (content.isBlank()) {
            throw new BadRequestException("답변 내용을 입력해주세요.");
        }
    }

    private void validateCanVote(Long userId, SopmodamRound round) {
        InternalUserDetails user = platformService.getInternalUser(userId);
        SopmodamMemberType memberType = memberPolicy.resolveType(user, round.getGeneration());
        memberPolicy.validateCanVote(memberType);
    }

    private SopmodamCurrentCardResponse toVoteCard(SopmodamRound round, SopmodamPhase phase, Long userId, LocalDateTime now) {
        boolean hasVoted = voteRetriever.findVotedQuestionId(round.getId(), userId).isPresent();
        boolean hasAnswered = answerRetriever.hasAnswered(round.getId(), userId);

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
            hasAnswered,
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
        boolean hasVoted = voteRetriever.findVotedQuestionId(round.getId(), userId).isPresent();
        boolean hasAnswered = answerRetriever.hasAnswered(round.getId(), userId);
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
            hasVoted,
            isCollecting,
            hasAnswered,
            isRevealed ? answerRetriever.countByRoundId(round.getId()) : null
        );
    }
}
