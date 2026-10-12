package org.sopt.makers.internal.sopmodam.service;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.exception.BadRequestException;
import org.sopt.makers.internal.exception.NotFoundException;
import org.sopt.makers.internal.sopmodam.domain.SopmodamQuestion;
import org.sopt.makers.internal.sopmodam.domain.SopmodamRound;
import org.sopt.makers.internal.sopmodam.repository.SopmodamQuestionRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SopmodamQuestionRetriever {

    private final SopmodamQuestionRepository questionRepository;

    public List<SopmodamQuestion> findAllByRoundId(Long roundId) {
        return questionRepository.findAllByRoundIdOrderByIdAsc(roundId);
    }

    public SopmodamQuestion findQuestionById(Long questionId) {
        return questionRepository.findById(questionId)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 질문입니다."));
    }

    public SopmodamQuestion findQuestionInRound(Long questionId, Long roundId) {
        return questionRepository.findByIdAndRoundId(questionId, roundId)
            .orElseThrow(() -> new NotFoundException("해당 회차에 존재하지 않는 질문입니다."));
    }

    public Optional<SopmodamQuestion> findFinalQuestion(SopmodamRound round) {
        if (round.getSelectedQuestionId() == null) {
            return Optional.empty();
        }
        return questionRepository.findByIdAndRoundId(round.getSelectedQuestionId(), round.getId());
    }

    public SopmodamQuestion findSelectedQuestionInRound(Long roundId) {
        return questionRepository.findByRoundIdAndIsSelectedTrue(roundId)
            .orElseThrow(() -> new BadRequestException("최종 질문이 선정되지 않았습니다."));
    }

    public Optional<SopmodamQuestion> findOptionalSelectedQuestionInRound(Long roundId) {
        return questionRepository.findByRoundIdAndIsSelectedTrue(roundId);
    }
}
