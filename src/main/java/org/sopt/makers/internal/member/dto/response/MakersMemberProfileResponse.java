package org.sopt.makers.internal.member.dto.response;

import java.util.List;

public record MakersMemberProfileResponse (
        Long id,
        String name,
        String profileImage,
        List<MemberSoptActivityResponse> activities,
        List<MemberCareerResponse> careers
){ }
