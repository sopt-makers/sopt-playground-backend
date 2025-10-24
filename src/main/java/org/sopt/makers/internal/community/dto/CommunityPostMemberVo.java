package org.sopt.makers.internal.community.dto;

import org.springframework.aot.hint.annotation.Reflective;

@Reflective
public record CommunityPostMemberVo(
        MemberVo member,
        CommunityPostVo post,
        CategoryVo category
) {
}
