package org.sopt.makers.internal.community.service.anonymous;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousProfileImage;
import org.sopt.makers.internal.community.domain.CommunityPost;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousNickname;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousPostProfile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AnonymousPostProfileService {

    private final AnonymousPostProfileModifier anonymousPostProfileModifier;
    private final AnonymousPostProfileRetriever anonymousPostProfileRetriever;
    private final AnonymousProfileImageRetriever anonymousProfileImageRetriever;
    private final AnonymousNicknameRetriever anonymousNicknameRetriever;

    public void createAnonymousPostProfile(Boolean isBlindWriter, Member member, CommunityPost post) {
        if (isBlindWriter) {
            List<AnonymousPostProfile> lastFourAnonymousPostProfiles = anonymousPostProfileRetriever.getTopByOrderByCreatedAt(4);
            List<AnonymousPostProfile> lastFiftyAnonymousPostProfiles = anonymousPostProfileRetriever.getTopByOrderByCreatedAt(50);
            List<Long> usedAnonymousProfileImageIds = lastFourAnonymousPostProfiles.stream()
                    .map(anonymousProfile -> anonymousProfile.getProfileImg().getId()).toList();
            List<AnonymousNickname> usedAnonymousNicknames = lastFiftyAnonymousPostProfiles.stream()
                    .map(AnonymousPostProfile::getNickname).toList();

            AnonymousNickname anonymousNickname = anonymousNicknameRetriever.findRandomAnonymousNickname(usedAnonymousNicknames);
            AnonymousProfileImage anonymousProfileImage = anonymousProfileImageRetriever.getAnonymousProfileImage(usedAnonymousProfileImageIds);
            anonymousPostProfileModifier.createAnonymousPostProfile(member, anonymousNickname, anonymousProfileImage, post);
        }
    }
}
