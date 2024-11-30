package org.sopt.makers.internal.service.community.anonymous;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.makers.internal.community.domain.CommunityPost;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousNickname;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousPostProfile;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousProfileImage;
import org.sopt.makers.internal.community.service.anonymous.*;
import org.sopt.makers.internal.domain.Member;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnonymousPostProfileServiceTest {

    @InjectMocks
    private AnonymousPostProfileService anonymousPostProfileService;

    @Mock
    private AnonymousPostProfileModifier anonymousPostProfileModifier;

    @Mock
    private AnonymousPostProfileRetriever anonymousPostProfileRetriever;

    @Mock
    private AnonymousProfileImageRetriever anonymousProfileImageRetriever;

    @Mock
    private AnonymousNicknameRetriever anonymousNicknameRetriever;

    @Test
    @DisplayName("isBlindWriter가 false일 경우 아무 작업도 수행되지 않는다.")
    void createAnonymousPostProfile_whenIsBlindWriterFalse() {
        // Given
        Boolean isBlindWriter = false;
        Member member = mock(Member.class);
        CommunityPost post = mock(CommunityPost.class);

        // When
        anonymousPostProfileService.createAnonymousPostProfile(isBlindWriter, member, post);

        // Then
        verifyNoInteractions(anonymousPostProfileModifier, anonymousPostProfileRetriever,
                anonymousProfileImageRetriever, anonymousNicknameRetriever);
    }

    @Test
    @DisplayName("isBlindWriter가 true일 경우 익명 게시물 프로필이 생성된다.")
    void createAnonymousPostProfile_whenIsBlindWriterTrue_withBuilder() {
        // Given
        Boolean isBlindWriter = true;
        Member member = mock(Member.class);
        CommunityPost post = mock(CommunityPost.class);

        // AnonymousPostProfile의 최근 4개 및 50개의 프로필 Mock 데이터 생성
        List<AnonymousPostProfile> lastFourProfiles = List.of(
                AnonymousPostProfile.builder()
                        .profileImg(AnonymousProfileImage.builder().id(1L).imageUrl("Image1").build())
                        .nickname(AnonymousNickname.builder().id(1L).nickname("Nickname1").build())
                        .build(),
                AnonymousPostProfile.builder()
                        .profileImg(AnonymousProfileImage.builder().id(2L).imageUrl("Image2").build())
                        .nickname(AnonymousNickname.builder().id(2L).nickname("Nickname2").build())
                        .build()
        );

        List<AnonymousPostProfile> lastFiftyProfiles = List.of(
                AnonymousPostProfile.builder()
                        .profileImg(AnonymousProfileImage.builder().id(1L).imageUrl("Image1").build())
                        .nickname(AnonymousNickname.builder().id(1L).nickname("Nickname1").build())
                        .build(),
                AnonymousPostProfile.builder()
                        .profileImg(AnonymousProfileImage.builder().id(2L).imageUrl("Image2").build())
                        .nickname(AnonymousNickname.builder().id(2L).nickname("Nickname2").build())
                        .build(),
                AnonymousPostProfile.builder()
                        .profileImg(AnonymousProfileImage.builder().id(3L).imageUrl("Image3").build())
                        .nickname(AnonymousNickname.builder().id(3L).nickname("Nickname3").build())
                        .build()
        );

        // Mock 객체 동작 설정
        when(anonymousPostProfileRetriever.getTopByOrderByCreatedAt(4)).thenReturn(lastFourProfiles);
        when(anonymousPostProfileRetriever.getTopByOrderByCreatedAt(50)).thenReturn(lastFiftyProfiles);

        // 최근 사용된 ID 및 닉네임 리스트 생성
        List<Long> usedImageIds = lastFourProfiles.stream()
                .map(profile -> profile.getProfileImg().getId()).toList();
        List<AnonymousNickname> usedNicknames = lastFiftyProfiles.stream()
                .map(AnonymousPostProfile::getNickname).toList();

        // Mock 반환값 설정
        AnonymousNickname randomNickname = AnonymousNickname.builder()
                .id(10L)
                .nickname("RandomNickname")
                .build();
        AnonymousProfileImage randomImage = AnonymousProfileImage.builder()
                .id(10L)
                .imageUrl("RandomImage")
                .build();

        when(anonymousNicknameRetriever.findRandomAnonymousNickname(usedNicknames)).thenReturn(randomNickname);
        when(anonymousProfileImageRetriever.getAnonymousProfileImage(usedImageIds)).thenReturn(randomImage);

        // When
        anonymousPostProfileService.createAnonymousPostProfile(isBlindWriter, member, post);

        // Then
        verify(anonymousPostProfileRetriever, times(1)).getTopByOrderByCreatedAt(4);
        verify(anonymousPostProfileRetriever, times(1)).getTopByOrderByCreatedAt(50);
        verify(anonymousNicknameRetriever, times(1)).findRandomAnonymousNickname(usedNicknames);
        verify(anonymousProfileImageRetriever, times(1)).getAnonymousProfileImage(usedImageIds);
        verify(anonymousPostProfileModifier, times(1)).createAnonymousPostProfile(member, randomNickname, randomImage, post);
    }
}
