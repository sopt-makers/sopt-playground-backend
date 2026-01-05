package org.sopt.makers.internal.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record QuestionReportRequest(
	@Schema(description = "신고 사유")
	String reason
) {
}
