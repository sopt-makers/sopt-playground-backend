package org.sopt.makers.internal.community.service.anonymous;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousProfileImage;
import org.sopt.makers.internal.community.domain.CommunityPost;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousNickname;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousPostProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnonymousPostProfileService {

    private static final int RECENT_NICKNAME_LIMIT = 50;

    private final AnonymousPostProfileModifier anonymousPostProfileModifier;
    private final AnonymousPostProfileRetriever anonymousPostProfileRetriever;
    private final AnonymousProfileImageRetriever anonymousProfileImageRetriever;
    private final AnonymousNicknameRetriever anonymousNicknameRetriever;

    @Transactional
    public void createAnonymousPostProfile(Member member, CommunityPost post) {
        List<AnonymousPostProfile> lastFiftyAnonymousPostProfiles = anonymousPostProfileRetriever.getTopByOrderByCreatedAt(RECENT_NICKNAME_LIMIT);
        List<AnonymousNickname> usedAnonymousNicknames = lastFiftyAnonymousPostProfiles.stream()
                .map(AnonymousPostProfile::getNickname).toList();

        AnonymousNickname anonymousNickname = anonymousNicknameRetriever.findRandomAnonymousNickname(usedAnonymousNicknames);
        AnonymousProfileImage anonymousProfileImage = anonymousProfileImageRetriever.getAnonymousProfileImage();
        anonymousPostProfileModifier.createAnonymousPostProfile(member, anonymousNickname, anonymousProfileImage, post);
    }
}
