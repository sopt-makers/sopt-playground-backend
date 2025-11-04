package org.sopt.makers.internal.community.dto.response;

import java.util.List;

import org.sopt.makers.internal.community.dto.MemberVo;

public record SopticlePostResponse(
        Long id,
        MemberVo member,
        String createdAt,
        String title,
        String content,
        List<String> images,
        String sopticleUrl
) {

}
