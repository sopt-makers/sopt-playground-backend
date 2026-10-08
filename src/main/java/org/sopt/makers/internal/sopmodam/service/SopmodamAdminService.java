package org.sopt.makers.internal.sopmodam.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.sopmodam.domain.SopmodamQuestion;
import org.sopt.makers.internal.sopmodam.domain.SopmodamRound;
import org.sopt.makers.internal.sopmodam.domain.enums.SopmodamPhase;
import org.sopt.makers.internal.sopmodam.dto.request.SopmodamRoundCreateRequest;
import org.sopt.makers.internal.sopmodam.dto.response.SopmodamQuestionResultResponse;
import org.sopt.makers.internal.sopmodam.dto.response.SopmodamRoundDetailResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 어드민 API 는 Platform 을 호출하지 않으므로 서비스 메서드 단위로 트랜잭션을 건다
@Service
@RequiredArgsConstructor
public class SopmodamAdminService {

    private final SopmodamRoundRetriever roundRetriever;
    private final SopmodamQuestionRetriever questionRetriever;
    private final SopmodamAnswerRetriever answerRetriever;
    private final SopmodamRoundModifier roundModifier;
    private final SopmodamQuestionModifier questionModifier;
    private final SopmodamRoundPolicy roundPolicy;

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

    @Transactional(readOnly = true)
    public SopmodamRoundDetailResponse getRound(Long roundId) {
        LocalDateTime now = LocalDateTime.now(KST);
        SopmodamRound round = roundRetriever.findRoundById(roundId);
        return buildRoundDetail(round, now);
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
