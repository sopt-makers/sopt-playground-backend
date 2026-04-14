package org.sopt.makers.internal.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "같은 기수 + 같은 파트 추천 멤버 응답")
public record SameGenerationAndPartRecommendResponse(
    List<SameGenerationAndPartMember> members
) {
    @Schema(description = "추천 멤버 정보")
    public record SameGenerationAndPartMember(
        Long id,
        String name,
        String profileImage,
        Integer generation,
        String part
    ) {}
}
