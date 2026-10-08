package org.sopt.makers.internal.sopmodam.domain.enums;

import java.time.LocalDateTime;

import org.sopt.makers.internal.sopmodam.domain.SopmodamRound;

public enum SopmodamPhase {

    SCHEDULED,
    VOTING,
    ANSWERING,
    REVEALED;

    // 일정 경계는 시작 포함, 종료 미포함으로 판단한다
    public static SopmodamPhase of(SopmodamRound round, LocalDateTime now) {
        if (now.isBefore(round.getVoteStartAt())) {
            return SCHEDULED;
        }
        if (now.isBefore(round.getVoteEndAt())) {
            return VOTING;
        }
        if (now.isBefore(round.getAnswerEndAt())) {
            return ANSWERING;
        }
        return REVEALED;
    }
}
