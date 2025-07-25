package org.sopt.makers.internal.member.dto.response;

import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.member.domain.Member;

public record MemberNameAndProfileImageResponse(
        Long id,
        String name,
        String profileImage
) {
//    public static MemberNameAndProfileImageResponse from(Member member) {
//        return new MemberNameAndProfileImageResponse(
//                member.getId(),
//                member.getName(),
//                member.getProfileImage()
//        );
//    }

    public static MemberNameAndProfileImageResponse from(InternalUserDetails userDetails) {
        if (userDetails == null) return null;
        return new MemberNameAndProfileImageResponse(
                userDetails.userId(),
                userDetails.name(),
                userDetails.profileImage()
        );
    }
}
