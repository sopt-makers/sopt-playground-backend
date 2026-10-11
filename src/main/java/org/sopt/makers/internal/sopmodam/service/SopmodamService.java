package org.sopt.makers.internal.sopmodam.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.exception.BadRequestException;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.external.platform.PlatformService;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.service.MemberRetriever;
import org.sopt.makers.internal.sopmodam.domain.SopmodamAnswer;
import org.sopt.makers.internal.sopmodam.domain.SopmodamQuestion;
import org.sopt.makers.internal.sopmodam.domain.SopmodamRound;
import org.sopt.makers.internal.sopmodam.domain.enums.SopmodamMemberType;
import org.sopt.makers.internal.sopmodam.domain.enums.SopmodamPhase;
import org.sopt.makers.internal.sopmodam.dto.request.SopmodamAnswerCreateRequest;
import org.sopt.makers.internal.sopmodam.dto.response.SopmodamAnswerCreateResponse;
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
    private final SopmodamVoteModifier voteModifier;
    private final SopmodamAnswerRetriever answerRetriever;
    private final SopmodamAnswerModifier answerModifier;
    private final SopmodamMemberPolicy memberPolicy;
    private final MemberRetriever memberRetriever;
    private final PlatformService platformService;

    private final ZoneId KST = ZoneId.of("Asia/Seoul");

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

    public SopmodamAnswerCreateResponse createAnswer(Long userId, Long roundId, SopmodamAnswerCreateRequest request) {
        validateAnswerContent(request.content());

        LocalDateTime now = LocalDateTime.now(KST);
        SopmodamRound round = roundRetriever.findRoundById(roundId);

        InternalUserDetails user = platformService.getInternalUser(userId);
        SopmodamMemberType memberType = memberPolicy.resolveType(user, round.getGeneration());
        memberPolicy.validateCanAnswer(memberType);

        if (SopmodamPhase.of(round, now) != SopmodamPhase.ANSWERING) {
            throw new BadRequestException("답변 모집 기간이 아닙니다.");
        }

        SopmodamQuestion selectedQuestion = questionRetriever.findSelectedQuestionInRound(roundId);
        answerRetriever.validateNotAnswered(selectedQuestion.getId(), userId);
        // 회원을 먼저 확인해 두면 답변 저장 시 무결성 위반은 유니크 제약뿐이다
        Member member = memberRetriever.findMemberById(userId);

        SopmodamAnswer answer = answerModifier.createAnswer(selectedQuestion, member, request.content());
        return SopmodamAnswerCreateResponse.from(answer);
    }

    private void validateAnswerContent(String content) {
        if (content == null || content.length() > 500) {
            throw new BadRequestException("답변 글자수가 초과되었습니다.");
        }
        String stripped = content.replaceAll("[\\s]", "");
        if (stripped.length() < 20) {
            throw new BadRequestException("답변 글자수가 너무 적습니다.");
        }
    }

    private void validateCanVote(Long userId, SopmodamRound round) {
        InternalUserDetails user = platformService.getInternalUser(userId);
        SopmodamMemberType memberType = memberPolicy.resolveType(user, round.getGeneration());
        memberPolicy.validateCanVote(memberType);
    }
}
