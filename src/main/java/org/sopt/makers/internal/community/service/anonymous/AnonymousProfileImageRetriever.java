package org.sopt.makers.internal.community.service.anonymous;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousProfileImage;
import org.sopt.makers.internal.community.repository.anonymous.AnonymousProfileImageRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnonymousProfileImageRetriever {

    private final AnonymousProfileImageRepository anonymousProfileImageRepository;

    private final Map<Long, AnonymousProfileImage> profileImageMap = new ConcurrentHashMap<>();
    private volatile boolean initialized = false;
    private final static Long MAKERS_LOGO_IMAGE_ID = 6L;

    private void ensureInitialized() {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    try {
                        List<AnonymousProfileImage> anonymousProfileImages =
                                anonymousProfileImageRepository.findAllByIdNot(MAKERS_LOGO_IMAGE_ID);
                        for (AnonymousProfileImage image : anonymousProfileImages) {
                            profileImageMap.put(image.getId(), image);
                        }
                        initialized = true;
                    } catch (Exception e) {
                        log.warn("익명 프로필 이미지 초기화 실패, 다음 요청 시 재시도합니다.", e);
                    }
                }
            }
        }
    }

    public AnonymousProfileImage getAnonymousProfileImage() {
        ensureInitialized();
        long randomImageNumber = ThreadLocalRandom.current().nextLong(1, 6);
        return profileImageMap.get(randomImageNumber);
    }
}
