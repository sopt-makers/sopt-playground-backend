package org.sopt.makers.internal.sopmodam.service;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.sopmodam.domain.SopmodamAnswer;
import org.sopt.makers.internal.sopmodam.repository.SopmodamAnswerRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SopmodamAnswerRetriever {

    private final SopmodamAnswerRepository answerRepository;

    public long countByRoundId(Long roundId) {
        return answerRepository.countByRoundId(roundId);
    }

    public boolean hasAnswered(Long roundId, Long memberId) {
        return answerRepository.existsByRoundIdAndMemberId(roundId, memberId);
    }

    public Optional<SopmodamAnswer> findMyAnswer(Long roundId, Long memberId) {
        return answerRepository.findByRoundIdAndMemberId(roundId, memberId);
    }
}
