package org.sopt.makers.internal.community.service;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.domain.CommunityPostLike;
import org.sopt.makers.internal.community.repository.CommunityPostLikeRepository;
import org.sopt.makers.internal.community.domain.CommunityPost;
import org.sopt.makers.internal.exception.ClientBadRequestException;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.sopt.makers.internal.community.repository.CommunityPostRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommunityPostRetriever {

    private final CommunityPostRepository communityPostRepository;
    private final CommunityPostLikeRepository communityPostLikeRepository;

    public CommunityPost findCommunityPostById(Long postId) {
        return communityPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundDBEntityException("존재하지 않는 게시글의 id값 입니다."));
    }

    public void checkExistsCommunityPostById(Long memberId) {
        if (!communityPostRepository.existsById(memberId)) {
            throw new NotFoundDBEntityException("존재하지 않는 게시글의 id값 입니다.");
        }
    }

    public void checkAlreadyLikedPost(Long memberId, Long postId) {
        if (Boolean.TRUE.equals(communityPostLikeRepository.existsByMemberIdAndPostId(memberId, postId))) {
            throw new ClientBadRequestException("이 게시물에는 이미 좋아요를 눌렀습니다.");
        }
    }

    public CommunityPostLike findCommunityPostLike(Long memberId, Long postId) {
        return communityPostLikeRepository.findCommunityPostLikeByMemberIdAndPostId(memberId, postId)
                .orElseThrow(() -> new NotFoundDBEntityException("이 게시물에는 아직 좋아요를 누르지 않았습니다."));
    }
}
