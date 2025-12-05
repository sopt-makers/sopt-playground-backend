package org.sopt.makers.internal.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.sopt.makers.internal.member.domain.enums.ServiceType;

import java.util.List;

public record TlMemberResponse(
        @Schema(required = true)
        Long id,
        @Schema(required = true)
        String name,
        String university,
        String profileImage,
        @Schema(required = true)
        List<MemberProfileResponse.MemberSoptActivityResponse> activities,
        String introduction,
        @Schema(required = true, description = "TL이 발표한 서비스 타입 (WEB 또는 APP)")
        ServiceType serviceType
) {
}
