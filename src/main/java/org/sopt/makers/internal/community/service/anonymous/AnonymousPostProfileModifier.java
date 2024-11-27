package org.sopt.makers.internal.community.service.anonymous;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.domain.AnonymousProfileImage;
import org.sopt.makers.internal.community.domain.CommunityPost;
import org.sopt.makers.internal.community.repository.anonymous.AnonymousPostProfileRepository;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.domain.community.AnonymousNickname;
import org.sopt.makers.internal.domain.community.AnonymousPostProfile;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnonymousPostProfileModifier {

    private final AnonymousPostProfileRepository anonymousPostProfileRepository;

    public void createAnonymousPostProfile(Member member, AnonymousNickname anonymousNickname, AnonymousProfileImage anonymousProfileImage, CommunityPost communityPost) {

        anonymousPostProfileRepository.save(AnonymousPostProfile.builder()
                .member(member)
                .nickname(anonymousNickname)
                .profileImg(anonymousProfileImage)
                .communityPost(communityPost)
                .build()
        );
    }
}
