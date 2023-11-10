package org.sopt.makers.internal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.sopt.makers.internal.dto.community.CategoryPostMemberDao;
import org.sopt.makers.internal.exception.ClientBadRequestException;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.sopt.makers.internal.repository.CategoryRepository;
import org.sopt.makers.internal.repository.CommunityQueryRepository;
import org.sopt.makers.internal.repository.MemberRepository;
import org.sopt.makers.internal.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class CommuntiyPostService {

    private final CommunityQueryRepository communityQueryRepository;
    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public List<CategoryPostMemberDao> getAllPosts(Long categoryId, Integer limit, Long cursor) {
        if(limit == null || limit >= 50) limit = 50;
        if(categoryId == null) {
            return communityQueryRepository.findAllPostByCursor(limit, cursor);
        } else {
            categoryRepository.findById(categoryId).orElseThrow(
                    () -> new ClientBadRequestException("존재하지 않는 categoryId입니다."));
            return communityQueryRepository.findAllParentCategoryPostByCursor(categoryId, limit, cursor);
        }
    }

    @Transactional(readOnly = true)
    public CategoryPostMemberDao getPostById(Long postId) {
        postRepository.findById(postId).orElseThrow(() -> new ClientBadRequestException("존재하지 않는 postId입니다."));
        return communityQueryRepository.getPostById(postId);
    }

    @Transactional
    public void increaseHit(Long postId, Long memberId) {
        val member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundDBEntityException("Member"));

        val post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundDBEntityException("Post"));

        // 5분 동안 메모리에 저장

        communityQueryRepository.updateHitsByPostId(postId);
    }
}
