package org.sopt.makers.internal.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Work preference recommendation response")
public record WorkPreferenceRecommendationResponse(
    @Schema(description = "작업 성향 등록 여부", required = true, example = "true")
    boolean hasWorkPreference,

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

        @Schema(description = "대학교")
        String university,

        @Schema(description = "37기 활동 정보", required = true)
        MemberSoptActivityResponse activity,

        @Schema(description = "작업 성향 정보", required = true)
        WorkPreferenceData workPreference
    ) {}

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

    @Schema(description = "SOPT 활동 정보")
    public record MemberSoptActivityResponse(
        @Schema(description = "활동 ID")
        Long id,

        @Schema(description = "기수", example = "37")
        Integer generation,

        @Schema(description = "파트", example = "서버")
        String part,

        @Schema(description = "팀")
        String team
    ) {}
}
