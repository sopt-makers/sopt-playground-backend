package org.sopt.makers.internal.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Work preference response")
public record WorkPreferenceResponse(
    @Schema(description = "작업 성향 정보")
    WorkPreferenceData workPreference
) {
    @Schema(description = "작업 성향 데이터")
    public record WorkPreferenceData(
        @Schema(description = "아이디어 스타일 (즉흥/숙고)")
        String ideationStyle,

        @Schema(description = "작업 시간 (아침/밤)")
        String workTime,

        @Schema(description = "커뮤니케이션 스타일 (몰아서/나눠서)")
        String communicationStyle,

        @Schema(description = "작업 장소 (카공/집콕)")
        String workPlace,

        @Schema(description = "피드백 스타일 (직설적/돌려서)")
        String feedbackStyle
    ) {}
}
