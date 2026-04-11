package org.sopt.makers.internal.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "멤버 추천 응답")
public record MemberRecommendResponse(
    List<RecommendedMember> members
) {
    @Schema(description = "추천 멤버 정보")
    public record RecommendedMember(

        Long id,
        String name,
        String profileImage,
        Integer generation,
        String part,
        RecommendType recommendType
    ) {}

    @Schema(description = "추천 기준 타입")
    public enum RecommendType {
        SAME_PART,       // 같은 파트
        SAME_CREW,       // 같은 모임
        SAME_MBTI,       // 같은 MBTI
        SAME_UNIVERSITY, // 같은 학교
        SAME_GENERATION  // 같은 기수
    }
}
