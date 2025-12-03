package org.sopt.makers.internal.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Work preference update request")
public record WorkPreferenceUpdateRequest(
    @Schema(description = "즉흥 or 숙고", example = "즉흥", allowableValues = {"즉흥", "숙고"})
    String ideationStyle,

    @Schema(description = "아침 or 밤", example = "아침", allowableValues = {"아침", "밤"})
    String workTime,

    @Schema(description = "몰아서 or 나눠서", example = "몰아서", allowableValues = {"몰아서", "나눠서"})
    String communicationStyle,

    @Schema(description = "카공 or 집콕", example = "카공", allowableValues = {"카공", "집콕"})
    String workPlace,

    @Schema(description = "직설적 or 돌려서", example = "직설적", allowableValues = {"직설적", "돌려서"})
    String feedbackStyle
) {
}
