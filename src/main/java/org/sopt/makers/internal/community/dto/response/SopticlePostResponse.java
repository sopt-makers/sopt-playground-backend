package org.sopt.makers.internal.community.dto.response;

import java.time.LocalDateTime;
import org.sopt.makers.internal.community.dto.MemberVo;

public record SopticlePostResponse(
        Long id,
        MemberVo member,
        LocalDateTime createdAt,
        String title,
        String content,
        String sopticleUrl
) {

}
