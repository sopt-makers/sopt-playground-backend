package org.sopt.makers.internal.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Work preference recommendation response")
public record WorkPreferenceRecommendationResponse(
    @Schema(description = "추천된 멤버 목록")
    List<RecommendedMember> recommendations
) {
    @Schema(description = "추천된 멤버 정보")
    public record RecommendedMember(
        @Schema(description = "멤버 ID", required = true)
        Long id,

        @Schema(description = "멤버 이름", required = true)
        String name,

        @Schema(description = "프로필 이미지")
        String profileImage,

        @Schema(description = "생년월일")
        String birthday,

        @Schema(description = "대학교")
        String university,

        @Schema(description = "MBTI")
        String mbti,

        @Schema(description = "작업 성향")
        WorkPreferenceData workPreference,

        @Schema(description = "활동 정보", required = true)
        List<MemberSoptActivityResponse> activities
    ) {}

    @Schema(description = "작업 성향 데이터")
    public record WorkPreferenceData(
        String ideationStyle,
        String workTime,
        String communicationStyle,
        String workPlace,
        String feedbackStyle
    ) {}

    @Schema(description = "SOPT 활동 정보")
    public record MemberSoptActivityResponse(
        Long id,
        Integer generation,
        String part,
        String team
    ){}
}
