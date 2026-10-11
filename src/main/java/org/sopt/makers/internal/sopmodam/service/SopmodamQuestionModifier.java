package org.sopt.makers.internal.sopmodam.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.sopmodam.domain.SopmodamQuestion;
import org.sopt.makers.internal.sopmodam.domain.SopmodamRound;
import org.sopt.makers.internal.sopmodam.repository.SopmodamQuestionRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SopmodamQuestionModifier {

    private final SopmodamQuestionRepository questionRepository;

    // 입력 순서대로 저장해 questionId 오름차순이 등록 순서가 되도록 한다
    public List<SopmodamQuestion> createQuestions(SopmodamRound round, List<String> contents) {
        List<SopmodamQuestion> questions = contents.stream()
            .map(content -> SopmodamQuestion.builder()
                .round(round)
                .content(content)
                .build())
            .toList();
        return questionRepository.saveAll(questions);
    }
}
