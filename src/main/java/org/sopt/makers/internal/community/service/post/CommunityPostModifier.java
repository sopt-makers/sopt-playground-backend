package org.sopt.makers.internal.community.service.post;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.makers.internal.community.dto.request.PostSaveRequest;
import org.sopt.makers.internal.community.repository.post.CommunityPostRepository;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.community.domain.CommunityPost;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommunityPostModifier {

    private final CommunityPostRepository communityPostRepository;

    // CREATE
    public CommunityPost createCommunityPost(Member member, PostSaveRequest request) {

        return communityPostRepository.save(CommunityPost.builder()
                .member(member)
                .categoryId(request.categoryId())
                .title(request.title())
                .content(request.content())
                .images(request.images())
                .isQuestion(request.isQuestion())
                .isBlindWriter(request.isBlindWriter())
                .sopticleUrl(request.link())
                .comments(new ArrayList<>())
                .build());
    }

    @Transactional
    public void increaseHitTransactional(Long postId) {
        communityPostRepository.increaseHitsDirect(postId);
    }
}
