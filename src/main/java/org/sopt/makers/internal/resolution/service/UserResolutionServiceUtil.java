package org.sopt.makers.internal.resolution.service;

import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.sopt.makers.internal.resolution.domain.UserResolution;
import org.sopt.makers.internal.resolution.repository.UserResolutionRepository;

public class UserResolutionServiceUtil {

    public static UserResolution findUserResolutionByMember(Member member, UserResolutionRepository userResolutionRepository) {
        return userResolutionRepository.findUserResolutionByMember(member)
                .orElseThrow(() -> new NotFoundDBEntityException("Non-exists resolution message"));
    }
}
