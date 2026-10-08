package org.sopt.makers.internal.sopmodam.dto.response;

import java.time.LocalDateTime;

import org.sopt.makers.internal.sopmodam.domain.SopmodamRound;
import org.sopt.makers.internal.sopmodam.domain.enums.SopmodamPhase;

public record SopmodamRoundSummaryResponse(
    Long roundId,
    Integer generation,
    String eventName,
    SopmodamPhase phase,
    LocalDateTime voteStartAt,
    LocalDateTime voteEndAt,
    LocalDateTime answerEndAt
) {
    public static SopmodamRoundSummaryResponse of(SopmodamRound round, SopmodamPhase phase) {
        return new SopmodamRoundSummaryResponse(
            round.getId(),
            round.getGeneration(),
            round.getEventName(),
            phase,
            round.getVoteStartAt(),
            round.getVoteEndAt(),
            round.getAnswerEndAt()
        );
    }
}
