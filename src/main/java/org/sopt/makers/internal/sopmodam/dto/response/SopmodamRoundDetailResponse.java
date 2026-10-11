package org.sopt.makers.internal.sopmodam.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import org.sopt.makers.internal.sopmodam.domain.SopmodamRound;
import org.sopt.makers.internal.sopmodam.domain.enums.SopmodamPhase;

public record SopmodamRoundDetailResponse(
    Long roundId,
    Integer generation,
    String eventName,
    SopmodamPhase phase,
    LocalDateTime voteStartAt,
    LocalDateTime voteEndAt,
    LocalDateTime answerEndAt,
    int totalVoteCount,
    long answerCount,
    boolean needsSelection,
    List<SopmodamQuestionResultResponse> questions
) {
    public static SopmodamRoundDetailResponse of(
        SopmodamRound round,
        SopmodamPhase phase,
        int totalVoteCount,
        long answerCount,
        boolean needsSelection,
        List<SopmodamQuestionResultResponse> questions
    ) {
        return new SopmodamRoundDetailResponse(
            round.getId(),
            round.getGeneration(),
            round.getEventName(),
            phase,
            round.getVoteStartAt(),
            round.getVoteEndAt(),
            round.getAnswerEndAt(),
            totalVoteCount,
            answerCount,
            needsSelection,
            questions
        );
    }
}
