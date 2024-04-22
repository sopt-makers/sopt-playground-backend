package org.sopt.makers.internal.community.service;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.domain.AnonymousProfileImage;
import org.sopt.makers.internal.community.repository.AnonymousProfileImageRepository;
import org.sopt.makers.internal.domain.community.AnonymousProfileImg;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnonymousProfileImageService {

    private final AnonymousProfileImageRepository anonymousProfileImageRepository;

    @Transactional(readOnly = true)
    public AnonymousProfileImage getRandomProfileImage(List<Long> excludes) {
        if (excludes.isEmpty() || excludes.size() >= AnonymousProfileImg.values().length) {
            return shuffle((long)(Math.random() * 5));
        }
        return filtered(excludes);
    }

    private AnonymousProfileImage filtered(List<Long> excludes) {
        List<AnonymousProfileImage> anonymousProfileImages = anonymousProfileImageRepository.findAll();

        return anonymousProfileImages.stream()
                .filter(index -> !excludes.contains(index.getId()))
                .findFirst()
                .orElse(null);
    }

    private AnonymousProfileImage shuffle(Long index) {
        return anonymousProfileImageRepository.findById(index).orElseThrow(
                () -> new NotFoundDBEntityException("존재하지 않는 익명 프로필 인덱스 값입니다")
        );
    }
}
