package org.sopt.makers.internal.community.service.anonymous;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousProfileImage;
import org.sopt.makers.internal.community.repository.anonymous.AnonymousProfileImageRepository;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

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

    public AnonymousProfileImage getAnonymousProfileImage() {
        long randomImageNumber = ThreadLocalRandom.current().nextLong(1, 6);
        return profileImageMap.get(randomImageNumber);
    }
}
