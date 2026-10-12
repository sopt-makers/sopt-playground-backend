package org.sopt.makers.internal.sopmodam.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.exception.BadRequestException;
import org.sopt.makers.internal.exception.ConflictException;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.external.platform.PlatformService;
import org.sopt.makers.internal.sopmodam.domain.SopmodamAnswer;
import org.sopt.makers.internal.sopmodam.domain.SopmodamQuestion;
import org.sopt.makers.internal.sopmodam.domain.SopmodamRound;
import org.sopt.makers.internal.sopmodam.domain.enums.SopmodamPhase;
import org.sopt.makers.internal.sopmodam.dto.request.SopmodamQuestionCreateRequest;
import org.sopt.makers.internal.sopmodam.dto.request.SopmodamQuestionUpdateRequest;
import org.sopt.makers.internal.sopmodam.dto.request.SopmodamQuestionCreateRequest;
import org.sopt.makers.internal.sopmodam.dto.request.SopmodamQuestionUpdateRequest;
import org.sopt.makers.internal.sopmodam.dto.request.SopmodamRoundCreateRequest;
import org.sopt.makers.internal.sopmodam.dto.request.SopmodamRoundUpdateRequest;
import org.sopt.makers.internal.sopmodam.dto.response.SopmodamAnswerItemResponse;
import org.sopt.makers.internal.sopmodam.dto.response.SopmodamAnswerListResponse;
import org.sopt.makers.internal.sopmodam.dto.response.SopmodamQuestionResultResponse;
import org.sopt.makers.internal.sopmodam.dto.response.SopmodamQuestionResponse;
import org.sopt.makers.internal.sopmodam.dto.response.SopmodamRoundDetailResponse;
import org.sopt.makers.internal.sopmodam.dto.response.SopmodamRoundSummaryResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 어드민 API 는 대부분 Platform 을 호출하지 않으므로 서비스 메서드 단위로 트랜잭션을 건다
// 답변 목록 조회처럼 Platform 호출이 필요한 메서드는 @Transactional 을 걸지 않는다
@Service
@RequiredArgsConstructor
public class SopmodamAdminService {

    private final SopmodamRoundRetriever roundRetriever;
    private final SopmodamQuestionRetriever questionRetriever;
    private final SopmodamAnswerRetriever answerRetriever;
    private final SopmodamRoundModifier roundModifier;
    private final SopmodamQuestionModifier questionModifier;
    private final SopmodamRoundPolicy roundPolicy;
    private final PlatformService platformService;

    private final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Transactional
    public SopmodamRoundDetailResponse createRound(SopmodamRoundCreateRequest request) {
        LocalDateTime now = LocalDateTime.now(KST);
        roundPolicy.validateScheduleOrder(request.voteStartAt(), request.voteEndAt(), request.answerEndAt());
        roundRetriever.validateNotOverlapping(request.voteStartAt(), request.answerEndAt());

        SopmodamRound round = roundModifier.createRound(
            request.generation(),
            request.eventName(),
            request.voteStartAt(),
            request.voteEndAt(),
            request.answerEndAt()
        );
        questionModifier.createQuestions(round, request.questions());
        return buildRoundDetail(round, now);
    }

    // 다음 회차가 시작된 지난 회차도 SopmodamPhase.of 가 REVEALED 를 돌려준다
    @Transactional(readOnly = true)
    public List<SopmodamRoundSummaryResponse> getRounds() {
        LocalDateTime now = LocalDateTime.now(KST);
        return roundRetriever.findAllRounds().stream()
            .map(round -> SopmodamRoundSummaryResponse.of(round, SopmodamPhase.of(round, now)))
            .toList();
    }

    @Transactional(readOnly = true)
    public SopmodamRoundDetailResponse getRound(Long roundId) {
        LocalDateTime now = LocalDateTime.now(KST);
        SopmodamRound round = roundRetriever.findRoundById(roundId);
        return buildRoundDetail(round, now);
    }

    @Transactional
    public SopmodamRoundDetailResponse selectQuestion(Long roundId, Long questionId) {
        LocalDateTime now = LocalDateTime.now(KST);
        SopmodamRound round = roundRetriever.findRoundById(roundId);

        SopmodamPhase phase = SopmodamPhase.of(round, now);
        if (phase == SopmodamPhase.SCHEDULED || phase == SopmodamPhase.VOTING) {
            throw new BadRequestException("투표가 아직 끝나지 않았습니다.");
        }
        if (phase == SopmodamPhase.REVEALED) {
            throw new BadRequestException("이미 답변 공개 단계입니다.");
        }

        if (answerRetriever.countByRoundId(roundId) > 0) {
            throw new ConflictException("이미 답변이 존재하는 회차입니다.");
        }

        SopmodamQuestion target = questionRetriever.findQuestionInRound(questionId, roundId);
        questionModifier.selectQuestion(roundId, target);

        return buildRoundDetail(round, now);
    }

    @Transactional
    public SopmodamRoundDetailResponse updateRound(Long roundId, SopmodamRoundUpdateRequest request) {
        LocalDateTime now = LocalDateTime.now(KST);
        SopmodamRound round = roundRetriever.findRoundById(roundId);
        roundPolicy.validateScheduleOrder(request.voteStartAt(), request.voteEndAt(), request.answerEndAt());
        roundPolicy.validateScheduleChange(round, request.voteStartAt(), request.voteEndAt(), now);
        roundRetriever.validateNotOverlapping(roundId, request.voteStartAt(), request.answerEndAt());

        roundModifier.updateRound(
            round,
            request.generation(),
            request.eventName(),
            request.voteStartAt(),
            request.voteEndAt(),
            request.answerEndAt()
        );
        return buildRoundDetail(round, now);
    }

    @Transactional
    public SopmodamRoundDetailResponse addQuestion(Long roundId, SopmodamQuestionCreateRequest request) {
        LocalDateTime now = LocalDateTime.now(KST);
        SopmodamRound round = roundRetriever.findRoundById(roundId);
        roundPolicy.validateQuestionEditable(round, now);

        questionModifier.createQuestions(round, List.of(request.content()));
        return buildRoundDetail(round, now);
    }

    @Transactional
    public SopmodamRoundDetailResponse updateQuestion(Long questionId, SopmodamQuestionUpdateRequest request) {
        LocalDateTime now = LocalDateTime.now(KST);
        SopmodamQuestion question = questionRetriever.findQuestionById(questionId);
        SopmodamRound round = question.getRound();
        roundPolicy.validateQuestionEditable(round, now);

        questionModifier.updateContent(question, request.content());
        return buildRoundDetail(round, now);
    }

    // 최종 질문이 선정돼야 답변이 달리므로, 선정 전 조회 시 400 을 반환한다
    public SopmodamAnswerListResponse getAnswers(Long roundId) {
        SopmodamRound round = roundRetriever.findRoundById(roundId);
        SopmodamQuestion selectedQuestion = questionRetriever.findSelectedQuestionInRound(roundId);
        List<SopmodamAnswer> answers = answerRetriever.findAllByRoundId(roundId);

        List<Long> memberIds = answers.stream().map(a -> a.getMember().getId()).toList();
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

    private SopmodamRoundDetailResponse buildRoundDetail(SopmodamRound round, LocalDateTime now) {
        SopmodamPhase phase = SopmodamPhase.of(round, now);
        List<SopmodamQuestion> questions = questionRetriever.findAllByRoundId(round.getId());
        long answerCount = answerRetriever.countByRoundId(round.getId());
        int totalVoteCount = questions.stream()
            .mapToInt(SopmodamQuestion::getVoteCount)
            .sum();

        return SopmodamRoundDetailResponse.of(
            round,
            phase,
            totalVoteCount,
            answerCount,
            needsSelection(phase, questions),
            questions.stream().map(SopmodamQuestionResultResponse::from).toList()
        );
    }

    // 투표가 끝났는데 최종 질문이 없고 최다 득표가 동률이면 운영자가 최종 질문을 지정해야 한다
    private boolean needsSelection(SopmodamPhase phase, List<SopmodamQuestion> questions) {
        if (phase == SopmodamPhase.SCHEDULED || phase == SopmodamPhase.VOTING) {
            return false;
        }
        if (questions.stream().anyMatch(SopmodamQuestion::isSelected)) {
            return false;
        }

        int maxVoteCount = questions.stream()
            .mapToInt(SopmodamQuestion::getVoteCount)
            .max()
            .orElse(0);
        long topQuestionCount = questions.stream()
            .filter(question -> question.getVoteCount() == maxVoteCount)
            .count();
        return topQuestionCount > 1;
    }
}
