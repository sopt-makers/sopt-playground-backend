package org.sopt.makers.internal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.makers.internal.dto.community.CategoryPostMemberDao;
import org.sopt.makers.internal.exception.ClientBadRequestException;
import org.sopt.makers.internal.repository.CategoryRepository;
import org.sopt.makers.internal.repository.CommunityQueryRepository;
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
}
