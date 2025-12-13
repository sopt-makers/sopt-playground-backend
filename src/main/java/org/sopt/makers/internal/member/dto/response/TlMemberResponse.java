package org.sopt.makers.internal.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.sopt.makers.internal.member.domain.enums.ServiceType;

import java.util.List;

public record TlMemberResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Long id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String name,
        String university,
        String profileImage,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<MemberProfileResponse.MemberSoptActivityResponse> activities,
        String introduction,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "TL이 발표한 서비스 타입 (WEB 또는 APP)")
        ServiceType serviceType,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "TL의 노션 자기소개 링크")
        String selfIntroduction,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "TL의 경선 자료 링크")
        String competitionData
) {
}
