package org.sopt.makers.internal.sopmodam.service;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.exception.NotFoundException;
import org.sopt.makers.internal.sopmodam.domain.SopmodamRound;
import org.sopt.makers.internal.sopmodam.repository.SopmodamRoundRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SopmodamRoundRetriever {

    private final SopmodamRoundRepository roundRepository;

    public SopmodamRound findRoundById(Long roundId) {
        return roundRepository.findById(roundId)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 솝모담 회차입니다."));
    }
}
