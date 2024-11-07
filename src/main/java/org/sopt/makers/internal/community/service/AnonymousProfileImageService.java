package org.sopt.makers.internal.community.service;

import org.sopt.makers.internal.community.domain.AnonymousProfileImage;
import org.sopt.makers.internal.community.repository.AnonymousProfileImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnonymousProfileImageService {

    private final AnonymousProfileImageRepository anonymousProfileImageRepository;
    private final static Long MAKERS_LOGO_IMAGE_ID = 6L;

    public AnonymousProfileImageService(AnonymousProfileImageRepository anonymousProfileImageRepository) {
        this.anonymousProfileImageRepository = anonymousProfileImageRepository;
        initializeProfileImageMap();
    }

    private static final Map<Long, AnonymousProfileImage> profileImageMap = new HashMap<>();

    @Transactional(readOnly = true)
    public AnonymousProfileImage getRandomProfileImage(List<Long> excludes) {
        if (excludes.isEmpty() || excludes.size() >= profileImageMap.size()) {
            return shuffle((long) (Math.random() * 5));
        }
        return filtered(excludes);
    }

    private AnonymousProfileImage filtered(List<Long> excludes) {
        return profileImageMap.keySet().stream()
                .filter(i -> !excludes.contains(i))
                .findFirst()
                .map(profileImageMap::get)
                .orElseGet(() -> shuffle((long) (Math.random() * 5)));
    }

    private AnonymousProfileImage shuffle(Long index) {
        return profileImageMap.get(index);
    }

    private void initializeProfileImageMap() {
        List<AnonymousProfileImage> anonymousProfileImages = anonymousProfileImageRepository.findAllByIdNot(MAKERS_LOGO_IMAGE_ID);
        for (AnonymousProfileImage image : anonymousProfileImages) {
            profileImageMap.put(image.getId(), image);
        }
    }
}
