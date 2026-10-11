package org.sopt.makers.internal.sopmodam.repository;

import org.sopt.makers.internal.sopmodam.domain.SopmodamAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SopmodamAnswerRepository extends JpaRepository<SopmodamAnswer, Long> {

    boolean existsByQuestionIdAndMemberId(Long questionId, Long memberId);
}
