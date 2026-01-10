package org.sopt.makers.internal.member.service;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.member.repository.QuestionReportRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuestionReportRetriever {

	private final QuestionReportRepository questionReportRepository;

	public boolean existsByQuestionIdAndReporterId(Long questionId, Long reporterId) {
		return questionReportRepository.existsByQuestionIdAndReporterId(questionId, reporterId);
	}
}
