package org.sopt.makers.internal.community.dto;

import org.sopt.makers.internal.member.domain.MemberCareer;
import org.sopt.makers.internal.member.domain.MemberSoptActivity;

public record MemberVo(
        Long id,
        String name,
        String profileImage,
        MemberSoptActivity activity,
        MemberCareer careers
) {}