package org.sopt.makers.internal.sopmodam.service;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.sopmodam.domain.SopmodamRound;
import org.sopt.makers.internal.sopmodam.repository.SopmodamRoundRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SopmodamRoundModifier {

    private final SopmodamRoundRepository roundRepository;

    public SopmodamRound createRound(
        Integer generation,
        String eventName,
        LocalDateTime voteStartAt,
        LocalDateTime voteEndAt,
        LocalDateTime answerEndAt
    ) {
        return roundRepository.save(SopmodamRound.builder()
            .generation(generation)
            .eventName(eventName)
            .voteStartAt(voteStartAt)
            .voteEndAt(voteEndAt)
            .answerEndAt(answerEndAt)
            .build());
    }

    public void updateRound(
        SopmodamRound round,
        Integer generation,
        String eventName,
        LocalDateTime voteStartAt,
        LocalDateTime voteEndAt,
        LocalDateTime answerEndAt
    ) {
        round.updateRound(generation, eventName, voteStartAt, voteEndAt, answerEndAt);
        roundRepository.save(round);
    }
}
