package org.sopt.makers.internal.sopmodam.service;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.sopmodam.repository.SopmodamAnswerRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SopmodamAnswerRetriever {

    private final SopmodamAnswerRepository answerRepository;

    public long countByRoundId(Long roundId) {
        return answerRepository.countByRoundId(roundId);
    }
}
