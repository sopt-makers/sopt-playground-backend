package org.sopt.makers.internal.community.service;

import org.sopt.makers.internal.community.repository.CommunityPostLikeRepository;
import org.sopt.makers.internal.domain.community.CommunityPost;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.sopt.makers.internal.community.repository.CommunityPostRepository;

public class CommunityPostServiceUtil {

    public static CommunityPost findCommunityPostById(CommunityPostRepository communityPostRepository, Long postId) {
        return communityPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundDBEntityException("존재하지 않는 게시글의 id값 입니다."));
    }

    public static Boolean isAlreadyLikePost(CommunityPostLikeRepository communityPostLikeRepository, Long memberId, Long postId) {
        return communityPostLikeRepository.existsByMemberIdAndPostId(memberId, postId);
    }

    public static void checkExistsCommunityPostById(CommunityPostRepository communityPostRepository, Long memberId) {
        if (!communityPostRepository.existsById(memberId)) {
            throw new NotFoundDBEntityException("존재하지 않는 게시글의 id값 입니다.");
        }
    }
}
