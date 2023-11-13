package org.sopt.makers.internal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.sopt.makers.internal.exception.ClientBadRequestException;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.sopt.makers.internal.repository.MemberRepository;
import org.sopt.makers.internal.repository.PostRepository;
import org.sopt.makers.internal.domain.community.CommunityPost;
import org.sopt.makers.internal.dto.community.CommunityPostMemberVo;
import org.sopt.makers.internal.dto.community.PostSaveRequest;
import org.sopt.makers.internal.mapper.CommunityResponseMapper;
import org.sopt.makers.internal.repository.community.CategoryRepository;
import org.sopt.makers.internal.repository.community.CommunityPostRepository;
import org.sopt.makers.internal.repository.community.CommunityQueryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class CommuntiyPostService {

    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;
    private final CommunityPostRepository communityPostRepository;
    private final CommunityQueryRepository communityQueryRepository;
    private final CommunityResponseMapper communityResponseMapper;

    @Transactional(readOnly = true)
    public List<CommunityPostMemberVo> getAllPosts(Long categoryId, Integer limit, Long cursor) {
        if(limit == null || limit >= 50) limit = 50;
        if(categoryId == null) {
            val posts = communityQueryRepository.findAllPostByCursor(limit, cursor);
            return posts.stream().map(communityResponseMapper::toPostVO).collect(Collectors.toList());
        } else {
            categoryRepository.findById(categoryId).orElseThrow(
                    () -> new ClientBadRequestException("존재하지 않는 categoryId입니다."));
            val posts = communityQueryRepository.findAllParentCategoryPostByCursor(categoryId, limit, cursor);
            return posts.stream().map(communityResponseMapper::toPostVO).collect(Collectors.toList());
        }
    }

    @Transactional(readOnly = true)
    public CommunityPostMemberVo getPostById(Long postId) {
        val postDao = communityQueryRepository.getPostById(postId);
        if(Objects.isNull(postDao)) throw new ClientBadRequestException("존재하지 않는 postId입니다.");
        return communityResponseMapper.toPostVO(postDao);
    }

    @Transactional
    public CommunityPost createPost(Long writerId, PostSaveRequest request) {
        val member = memberRepository.findById(writerId)
                .orElseThrow(() -> new NotFoundDBEntityException("Member"));
        val category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new NotFoundDBEntityException("Category"));
        return communityPostRepository.save(CommunityPost.builder()
                .member(member)
                .categoryId(request.categoryId())
                .title(request.title())
                .content(request.content())
                .hits(0)
                .images(request.images())
                .isQuestion(request.isQuestion())
                .createdAt(LocalDateTime.now())
                .comments(new ArrayList<>())
                .build());
    }

    @Transactional
    public void deletePost(Long writerId, Long postId) {
        val member = memberRepository.findById(writerId)
                .orElseThrow(() -> new NotFoundDBEntityException("Member"));
        val post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundDBEntityException("Community Post"));

        if (!Objects.equals(member.getId(), post.getMember().getId())) {
            throw new ClientBadRequestException("삭제 권한이 없는 유저입니다.");
        }

        communityPostRepository.delete(post);
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
