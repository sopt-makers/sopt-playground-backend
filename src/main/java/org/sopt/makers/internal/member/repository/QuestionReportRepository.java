package org.sopt.makers.internal.member.repository;

import org.sopt.makers.internal.member.domain.QuestionReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionReportRepository extends JpaRepository<QuestionReport, Long> {

	boolean existsByQuestionIdAndReporterId(Long questionId, Long reporterId);
}
