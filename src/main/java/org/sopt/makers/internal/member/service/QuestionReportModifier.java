package org.sopt.makers.internal.member.service;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.member.domain.QuestionReport;
import org.sopt.makers.internal.member.repository.QuestionReportRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuestionReportModifier {

	private final QuestionReportRepository questionReportRepository;

	public QuestionReport createReport(Long questionId, Long reporterId, String reason) {
		return questionReportRepository.save(QuestionReport.builder()
			.questionId(questionId)
			.reporterId(reporterId)
			.reason(reason)
			.build());
	}
}
