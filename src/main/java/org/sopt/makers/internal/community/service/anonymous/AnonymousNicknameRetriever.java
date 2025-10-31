package org.sopt.makers.internal.community.service.anonymous;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.repository.anonymous.AnonymousNicknameRepository;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousNickname;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    public void validateAnonymousNicknames(String[] nicknames) {
        if (nicknames == null || nicknames.length == 0) {
            return;
        }

        List<String> nicknameList = Arrays.asList(nicknames);
        List<AnonymousNickname> foundNicknames = anonymousNicknameRepository.findAllByNicknameIn(nicknameList);

        Set<String> foundNicknameSet = foundNicknames.stream()
                .map(AnonymousNickname::getNickname)
                .collect(Collectors.toSet());

        for (String nickname : nicknames) {
            if (!foundNicknameSet.contains(nickname)) {
                throw new NotFoundDBEntityException("존재하지 않는 익명 닉네임입니다: " + nickname);
            }
        }
    }
}
