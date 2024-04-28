package org.sopt.makers.internal.community.service;

import org.sopt.makers.internal.domain.community.AnonymousNickname;
import org.sopt.makers.internal.repository.community.AnonymousNicknameRepository;

import java.util.List;

public class AnonymousNicknameServiceUtil {

    public static AnonymousNickname getRandomNickname(AnonymousNicknameRepository anonymousNicknameRepository, List<AnonymousNickname> excludes) {
        if (excludes.isEmpty()) {
            return anonymousNicknameRepository.findRandomOne();
        }
        return anonymousNicknameRepository.findRandomOneByIdNotIn(
                excludes.stream().map(AnonymousNickname::getId).toList());
    }
}
