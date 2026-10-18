package org.sopt.makers.internal.sopmodam.service;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.common.util.ConstraintViolationUtil;
import org.sopt.makers.internal.exception.ConflictException;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.sopmodam.domain.SopmodamAnswer;
import org.sopt.makers.internal.sopmodam.domain.SopmodamQuestion;
import org.sopt.makers.internal.sopmodam.repository.SopmodamAnswerRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SopmodamAnswerModifier {

    private static final String ANSWER_QUESTION_MEMBER_UNIQUE_CONSTRAINT = "uk_sopmodam_answer_question_member";

    private final SopmodamAnswerRepository answerRepository;

    @Transactional
    public SopmodamAnswer updateAnswer(SopmodamAnswer answer, String content) {
        answer.updateContent(content);
        return answerRepository.save(answer);
    }

    @Transactional
    public SopmodamAnswer createAnswer(SopmodamQuestion question, Member member, String content) {
        SopmodamAnswer answer = SopmodamAnswer.builder()
            .question(question)
            .member(member)
            .content(content)
            .build();

        try {
            return answerRepository.saveAndFlush(answer);
        } catch (DataIntegrityViolationException exception) {
            if (ConstraintViolationUtil.isViolated(exception, ANSWER_QUESTION_MEMBER_UNIQUE_CONSTRAINT)) {
                throw new ConflictException("이미 답변하셨습니다.");
            }
            throw exception;
        }
    }
}
