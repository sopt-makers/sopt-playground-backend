package org.sopt.makers.internal.sopmodam.service;

import java.time.LocalDateTime;

import org.sopt.makers.internal.exception.BadRequestException;
import org.sopt.makers.internal.sopmodam.domain.SopmodamRound;
import org.sopt.makers.internal.sopmodam.domain.enums.SopmodamPhase;
import org.springframework.stereotype.Component;

@Component
public class SopmodamRoundPolicy {

    public void validateScheduleOrder(LocalDateTime voteStartAt, LocalDateTime voteEndAt, LocalDateTime answerEndAt) {
        if (!voteStartAt.isBefore(voteEndAt) || !voteEndAt.isBefore(answerEndAt)) {
            throw new BadRequestException("일정은 투표 시작 < 투표 마감 < 답변 마감 순서여야 합니다.");
        }
    }

    // 이미 지난 일정 지점은 바꿀 수 없다. 진행 단계는 수정 전 회차 일정으로 판단한다
    public void validateScheduleChange(
        SopmodamRound round,
        LocalDateTime voteStartAt,
        LocalDateTime voteEndAt,
        LocalDateTime now
    ) {
        SopmodamPhase phase = SopmodamPhase.of(round, now);
        if (phase != SopmodamPhase.SCHEDULED && !round.getVoteStartAt().isEqual(voteStartAt)) {
            throw new BadRequestException("투표가 시작된 회차는 투표 시작 시각을 변경할 수 없습니다.");
        }
        boolean isVoteEnded = phase == SopmodamPhase.ANSWERING || phase == SopmodamPhase.REVEALED;
        if (isVoteEnded && !round.getVoteEndAt().isEqual(voteEndAt)) {
            throw new BadRequestException("투표가 종료된 회차는 투표 마감 시각을 변경할 수 없습니다.");
        }
    }
}
