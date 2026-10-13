package org.sopt.makers.internal.sopmodam.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.exception.ConflictException;
import org.sopt.makers.internal.exception.NotFoundException;
import org.sopt.makers.internal.sopmodam.domain.SopmodamAnswer;
import org.sopt.makers.internal.sopmodam.repository.SopmodamAnswerRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SopmodamAnswerRetriever {

    private final SopmodamAnswerRepository answerRepository;

    public void validateNotAnswered(Long questionId, Long memberId) {
        if (answerRepository.existsByQuestionIdAndMemberId(questionId, memberId)) {
            throw new ConflictException("이미 답변하셨습니다.");
        }
    }
    public long countByRoundId(Long roundId) {
        return answerRepository.countByRoundId(roundId);
    }

    public boolean hasAnswered(Long roundId, Long memberId) {
        return answerRepository.existsByRoundIdAndMemberId(roundId, memberId);
    }

    public SopmodamAnswer findAnswerById(Long answerId) {
        return answerRepository.findByIdWithDetails(answerId)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 답변입니다."));
    }

    public List<SopmodamAnswer> findAllByRoundId(Long roundId) {
        return answerRepository.findAllByRoundIdOrderByUpdatedAtAscIdAsc(roundId);
    }
}
