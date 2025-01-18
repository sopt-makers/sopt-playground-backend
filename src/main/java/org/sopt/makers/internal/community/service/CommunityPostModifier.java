package org.sopt.makers.internal.community.service;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.controller.dto.request.PostSaveRequest;
import org.sopt.makers.internal.community.repository.CommunityPostRepository;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.community.domain.CommunityPost;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@RequiredArgsConstructor
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
                .comments(new ArrayList<>())
                .build());
    }
}
