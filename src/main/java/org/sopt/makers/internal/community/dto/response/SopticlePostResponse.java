package org.sopt.makers.internal.community.dto.response;

import org.sopt.makers.internal.community.dto.MemberVo;

public record SopticlePostResponse(
        Long id,
        MemberVo member,
        String createdAt,
        String title,
        String content,
        String[] images,
        String sopticleUrl
) {

}
