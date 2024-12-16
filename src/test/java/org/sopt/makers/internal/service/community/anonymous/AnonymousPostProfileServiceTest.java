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
    @DisplayName("isBlindWriter가 true일 경우 익명 게시물 프로필이 생성된다.")
    void createAnonymousPostProfile_whenIsBlindWriterTrue() {
        // Given
        Member member = mock(Member.class);
        CommunityPost post = mock(CommunityPost.class);

        List<AnonymousPostProfile> lastFourProfiles = createAnonymousPostProfiles(List.of(
                new ProfileData(1L, "Image1", 1L, "Nickname1"),
                new ProfileData(2L, "Image2", 2L, "Nickname2")
        ));

        List<AnonymousPostProfile> lastFiftyProfiles = createAnonymousPostProfiles(List.of(
                new ProfileData(1L, "Image1", 1L, "Nickname1"),
                new ProfileData(2L, "Image2", 2L, "Nickname2"),
                new ProfileData(3L, "Image3", 3L, "Nickname3")
        ));

        when(anonymousPostProfileRetriever.getTopByOrderByCreatedAt(4)).thenReturn(lastFourProfiles);
        when(anonymousPostProfileRetriever.getTopByOrderByCreatedAt(50)).thenReturn(lastFiftyProfiles);

        List<Long> usedImageIds = extractProfileImageIds(lastFourProfiles);
        List<AnonymousNickname> usedNicknames = extractNicknames(lastFiftyProfiles);

        AnonymousNickname randomNickname = createAnonymousNickname(10L, "RandomNickname");
        AnonymousProfileImage randomImage = createAnonymousProfileImage(10L, "RandomImage");

        when(anonymousNicknameRetriever.findRandomAnonymousNickname(usedNicknames)).thenReturn(randomNickname);
        when(anonymousProfileImageRetriever.getAnonymousProfileImage(usedImageIds)).thenReturn(randomImage);

        // When
        anonymousPostProfileService.createAnonymousPostProfile(member, post);

        // Then
        verify(anonymousPostProfileRetriever, times(1)).getTopByOrderByCreatedAt(4);
        verify(anonymousPostProfileRetriever, times(1)).getTopByOrderByCreatedAt(50);
        verify(anonymousNicknameRetriever, times(1)).findRandomAnonymousNickname(usedNicknames);
        verify(anonymousProfileImageRetriever, times(1)).getAnonymousProfileImage(usedImageIds);
        verify(anonymousPostProfileModifier, times(1))
                .createAnonymousPostProfile(member, randomNickname, randomImage, post);
    }

    private List<AnonymousPostProfile> createAnonymousPostProfiles(List<ProfileData> data) {
        return data.stream()
                .map(d -> AnonymousPostProfile.builder()
                        .profileImg(createAnonymousProfileImage(d.profileImageId, d.profileImageUrl))
                        .nickname(createAnonymousNickname(d.nicknameId, d.nickname))
                        .build())
                .toList();
    }

    private AnonymousNickname createAnonymousNickname(Long id, String nickname) {
        return AnonymousNickname.builder()
                .id(id)
                .nickname(nickname)
                .build();
    }

    private AnonymousProfileImage createAnonymousProfileImage(Long id, String imageUrl) {
        return AnonymousProfileImage.builder()
                .id(id)
                .imageUrl(imageUrl)
                .build();
    }

    private List<Long> extractProfileImageIds(List<AnonymousPostProfile> profiles) {
        return profiles.stream()
                .map(profile -> profile.getProfileImg().getId())
                .toList();
    }

    private List<AnonymousNickname> extractNicknames(List<AnonymousPostProfile> profiles) {
        return profiles.stream()
                .map(AnonymousPostProfile::getNickname)
                .toList();
    }

    private record ProfileData(Long profileImageId, String profileImageUrl, Long nicknameId, String nickname) {
    }
}
