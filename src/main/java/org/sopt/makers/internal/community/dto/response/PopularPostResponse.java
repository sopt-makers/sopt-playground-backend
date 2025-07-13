package org.sopt.makers.internal.community.dto.response;

import org.sopt.makers.internal.member.dto.response.MemberNameAndProfileImageResponse;

public record PopularPostResponse(
        Long id,
        String category,
        String title,
        MemberNameAndProfileImageResponse member,
        Integer hits
) {
}