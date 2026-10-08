package org.sopt.makers.internal.sopmodam.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.exception.NotFoundException;
import org.sopt.makers.internal.sopmodam.domain.SopmodamQuestion;
import org.sopt.makers.internal.sopmodam.repository.SopmodamQuestionRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SopmodamQuestionRetriever {

    private final SopmodamQuestionRepository questionRepository;

    public List<SopmodamQuestion> findAllByRoundId(Long roundId) {
        return questionRepository.findAllByRoundIdOrderByIdAsc(roundId);
    }

    public SopmodamQuestion findQuestionInRound(Long questionId, Long roundId) {
        return questionRepository.findByIdAndRoundId(questionId, roundId)
            .orElseThrow(() -> new NotFoundException("해당 회차에 존재하지 않는 질문입니다."));
    }
}
