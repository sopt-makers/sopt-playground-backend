package org.sopt.makers.internal.sopmodam.service;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.exception.ConflictException;
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
}
