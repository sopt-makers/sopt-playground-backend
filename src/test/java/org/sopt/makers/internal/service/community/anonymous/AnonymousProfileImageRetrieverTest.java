package org.sopt.makers.internal.service.community.anonymous;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousProfileImage;
import org.sopt.makers.internal.community.repository.anonymous.AnonymousProfileImageRepository;
import org.sopt.makers.internal.community.service.anonymous.AnonymousProfileImageRetriever;
import org.sopt.makers.internal.exception.BusinessLogicException;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnonymousProfileImageRetrieverTest {

    @InjectMocks
    private AnonymousProfileImageRetriever anonymousProfileImageRetriever;

    @Mock
    private AnonymousProfileImageRepository anonymousProfileImageRepository;

    private static final Long MAKERS_LOGO_IMAGE_ID = 6L;

    @Test
    @DisplayName("initializeProfileImageMap 호출 시 profileImageMap이 올바르게 초기화된다.")
    void initializeProfileImageMap_success() {
        // Given
        List<AnonymousProfileImage> dummyImages = List.of(
                AnonymousProfileImage.builder().id(1L).imageUrl("Image1").build(),
                AnonymousProfileImage.builder().id(2L).imageUrl("Image2").build(),
                AnonymousProfileImage.builder().id(3L).imageUrl("Image3").build(),
                AnonymousProfileImage.builder().id(4L).imageUrl("Image4").build(),
                AnonymousProfileImage.builder().id(5L).imageUrl("Image5").build()
        );
        when(anonymousProfileImageRepository.findAllByIdNot(MAKERS_LOGO_IMAGE_ID)).thenReturn(dummyImages);

        // When
        anonymousProfileImageRetriever.initializeProfileImageMap();

        // Then
        verify(anonymousProfileImageRepository, times(1)).findAllByIdNot(MAKERS_LOGO_IMAGE_ID);
    }

    @RepeatedTest(10)
    @DisplayName("recentUsedAnonymousProfileImageIds가 비어있을 때 랜덤 이미지를 반환한다.")
    void getAnonymousProfileImage_randomSelection() {
        // Given
        List<AnonymousProfileImage> dummyImages = List.of(
                AnonymousProfileImage.builder().id(1L).imageUrl("Image1").build(),
                AnonymousProfileImage.builder().id(2L).imageUrl("Image2").build(),
                AnonymousProfileImage.builder().id(3L).imageUrl("Image3").build(),
                AnonymousProfileImage.builder().id(4L).imageUrl("Image4").build(),
                AnonymousProfileImage.builder().id(5L).imageUrl("Image5").build()
        );
        when(anonymousProfileImageRepository.findAllByIdNot(6L)).thenReturn(dummyImages);
        anonymousProfileImageRetriever.initializeProfileImageMap();

        // When
        AnonymousProfileImage randomImage = anonymousProfileImageRetriever.getAnonymousProfileImage(Collections.emptyList());

        // Then
        assertThat(randomImage.getId()).isBetween(1L, 5L);
    }

    @Test
    @DisplayName("recentUsedAnonymousProfileImageIds에 포함되지 않은 이미지를 반환한다.")
    void getAnonymousProfileImage_excludeRecentIds() {
        // Given
        List<AnonymousProfileImage> dummyImages = List.of(
                AnonymousProfileImage.builder().id(1L).imageUrl("Image1").build(),
                AnonymousProfileImage.builder().id(2L).imageUrl("Image2").build(),
                AnonymousProfileImage.builder().id(3L).imageUrl("Image3").build(),
                AnonymousProfileImage.builder().id(4L).imageUrl("Image4").build(),
                AnonymousProfileImage.builder().id(5L).imageUrl("Image5").build()
        );
        when(anonymousProfileImageRepository.findAllByIdNot(6L)).thenReturn(dummyImages);
        anonymousProfileImageRetriever.initializeProfileImageMap();
        List<Long> recentIds = List.of(1L, 2L, 3L, 4L);

        // When
        AnonymousProfileImage image = anonymousProfileImageRetriever.getAnonymousProfileImage(recentIds);

        // Then
        assertThat(image.getId()).isIn(5L);
        assertThat(image.getId()).isNotIn(recentIds);
    }

    @Test
    @DisplayName("모든 이미지 ID가 recentUsedAnonymousProfileImageIds에 포함되었을 때 예외를 던진다.")
    void getAnonymousProfileImage_allIdsExcluded() {
        // Given
        List<AnonymousProfileImage> dummyImages = List.of(
                AnonymousProfileImage.builder().id(1L).imageUrl("Image1").build(),
                AnonymousProfileImage.builder().id(2L).imageUrl("Image2").build(),
                AnonymousProfileImage.builder().id(3L).imageUrl("Image3").build(),
                AnonymousProfileImage.builder().id(4L).imageUrl("Image4").build(),
                AnonymousProfileImage.builder().id(5L).imageUrl("Image5").build()
        );
        when(anonymousProfileImageRepository.findAllByIdNot(6L)).thenReturn(dummyImages);
        anonymousProfileImageRetriever.initializeProfileImageMap();
        List<Long> recentIds = List.of(1L, 2L, 3L, 4L, 5L);

        // When & Then
        assertThatThrownBy(() -> anonymousProfileImageRetriever.getAnonymousProfileImage(recentIds))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessage("존재하지 않는 익명 프로필 ID 입니다.");
    }
}
