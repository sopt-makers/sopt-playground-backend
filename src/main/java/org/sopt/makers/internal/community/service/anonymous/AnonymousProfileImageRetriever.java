package org.sopt.makers.internal.community.service.anonymous;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.domain.AnonymousProfileImage;
import org.sopt.makers.internal.community.repository.anonymous.AnonymousProfileImageRepository;
import org.sopt.makers.internal.exception.BusinessLogicException;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AnonymousProfileImageRetriever {

    private final AnonymousProfileImageRepository anonymousProfileImageRepository;

    private static final Map<Long, AnonymousProfileImage> profileImageMap = new HashMap<>();
    private final static Long MAKERS_LOGO_IMAGE_ID = 6L;

    @PostConstruct
    public void initializeProfileImageMap() {
        List<AnonymousProfileImage> anonymousProfileImages = anonymousProfileImageRepository.findAllByIdNot(MAKERS_LOGO_IMAGE_ID);
        for (AnonymousProfileImage image : anonymousProfileImages) {
            profileImageMap.put(image.getId(), image);
        }
    }

    public AnonymousProfileImage getAnonymousProfileImage(List<Long> recentUsedAnonymousProfileImageIds) {
        if (recentUsedAnonymousProfileImageIds.isEmpty()) {
            long randomImageNumber = (long) ((Math.random() * 5) + 1);
            return profileImageMap.get(randomImageNumber);
        }

        return profileImageMap.keySet().stream()
                .filter(id -> !recentUsedAnonymousProfileImageIds.contains(id))
                .findFirst()
                .map(profileImageMap::get)
                .orElseThrow(() -> new BusinessLogicException("존재하지 않는 익명 프로필 ID 입니다."));
    }
}
