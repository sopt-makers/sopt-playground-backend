package org.sopt.makers.internal.community.service.anonymous;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.repository.anonymous.AnonymousNicknameRepository;
import org.sopt.makers.internal.domain.community.AnonymousNickname;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AnonymousNicknameRetriever {

    private final AnonymousNicknameRepository anonymousNicknameRepository;

    public AnonymousNickname findRandomAnonymousNickname(List<AnonymousNickname> recentUsedAnonymousNicknames) {
        if (recentUsedAnonymousNicknames.isEmpty()) {
            return anonymousNicknameRepository.findRandomOne();
        }

        return anonymousNicknameRepository.findRandomOneByIdNotIn(
                recentUsedAnonymousNicknames.stream().map(AnonymousNickname::getId).toList());
    }
}
