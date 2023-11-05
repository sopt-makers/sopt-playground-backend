package org.sopt.makers.internal.dto.community;

import org.sopt.makers.internal.domain.MemberCareer;
import org.sopt.makers.internal.domain.MemberSoptActivity;

public record MemberVo(
        Long id,
        String name,
        String profileImage,
        MemberSoptActivity activity,
        MemberCareer careers
) {}