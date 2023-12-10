package org.sopt.makers.internal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.sopt.makers.internal.domain.community.CommunityPost;
import org.sopt.makers.internal.domain.community.ReportPost;
import org.sopt.makers.internal.dto.community.*;
import org.sopt.makers.internal.exception.ClientBadRequestException;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.sopt.makers.internal.mapper.CommunityMapper;
import org.sopt.makers.internal.mapper.CommunityResponseMapper;
import org.sopt.makers.internal.repository.MemberRepository;
import org.sopt.makers.internal.repository.PostRepository;
import org.sopt.makers.internal.repository.community.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
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
    private final ReportPostRepository reportPostRepository;
    private final CommunityPostRepository communityPostRepository;
    private final DeletedCommunityPostRepository deletedCommunityPostRepository;
    private final DeletedCommunityCommentRepository deletedCommunityCommentRepository;
    private final CommunityMapper communityMapper;
    private final CommunityQueryRepository communityQueryRepository;
    private final CommunityResponseMapper communityResponseMapper;

    private final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Transactional(readOnly = true)
    public List<CommunityPostMemberVo> getAllPosts(Long categoryId, Integer limit, Long cursor) {
        if(limit == null || limit >= 50) limit = 50;
        if(categoryId == null) {
            val posts = communityQueryRepository.findAllPostByCursor(limit, cursor);
            return posts.stream().map(communityResponseMapper::toCommunityVo).collect(Collectors.toList());
        } else {
            categoryRepository.findById(categoryId).orElseThrow(
                    () -> new ClientBadRequestException("존재하지 않는 categoryId입니다."));
            val posts = communityQueryRepository.findAllParentCategoryPostByCursor(categoryId, limit, cursor);
            return posts.stream().map(communityResponseMapper::toCommunityVo).collect(Collectors.toList());
        }
    }

    @Transactional(readOnly = true)
    public CommunityPostMemberVo getPostById(Long postId) {
        val postDao = communityQueryRepository.getPostById(postId);
        if(Objects.isNull(postDao)) throw new ClientBadRequestException("존재하지 않는 postId입니다.");
        return communityResponseMapper.toCommunityVo(postDao);
    }

    @Transactional
    public PostSaveResponse createPost(Long writerId, PostSaveRequest request) {
        val member = memberRepository.findById(writerId)
                .orElseThrow(() -> new NotFoundDBEntityException("Is not a Member"));
        val category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new NotFoundDBEntityException("Is not a categoryId"));
        val post = communityPostRepository.save(CommunityPost.builder()
                .member(member)
                .categoryId(request.categoryId())
                .title(request.title())
                .content(request.content())
                .hits(0)
                .images(request.images())
                .isQuestion(request.isQuestion())
                .isBlindWriter(request.isBlindWriter())
                .createdAt(LocalDateTime.now(KST))
                .comments(new ArrayList<>())
                .build());
        return communityResponseMapper.toPostSaveResponse(post);
    }

    @Transactional
    public PostUpdateResponse updatePost(Long writerId, PostUpdateRequest request) {
        val member = memberRepository.findById(writerId)
                .orElseThrow(() -> new NotFoundDBEntityException("Is not a Member"));
        val post = postRepository.findById(request.postId()).orElseThrow(
                () -> new NotFoundDBEntityException("Is not a exist postId"));
        val category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new NotFoundDBEntityException("Is not a categoryId"));

        if (!Objects.equals(member.getId(), post.getMember().getId())) {
            throw new ClientBadRequestException("수정 권한이 없는 유저입니다.");
        }

        communityPostRepository.save(CommunityPost.builder()
                .id(request.postId())
                .member(member)
                .categoryId(request.categoryId())
                .title(request.title())
                .content(request.content())
                .hits(post.getHits())
                .images(request.images())
                .isQuestion(request.isQuestion())
                .isBlindWriter(request.isBlindWriter())
                .createdAt(post.getCreatedAt())
                .updatedAt(LocalDateTime.now(KST))
                .build());
        return communityResponseMapper.toPostUpdateResponse(post);
    }

    @Transactional
    public void deletePost(Long postId, Long writerId) {
        val member = memberRepository.findById(writerId)
                .orElseThrow(() -> new NotFoundDBEntityException("Is not a Member"));
        val post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundDBEntityException("Is not a categoryId"));

        if (!Objects.equals(member.getId(), post.getMember().getId())) {
            throw new ClientBadRequestException("삭제 권한이 없는 유저입니다.");
        }

        val deletedPost = communityMapper.toDeleteCommunityPost(post);
        deletedCommunityPostRepository.save(deletedPost);
        post.getComments().stream().map(communityMapper::toDeleteCommunityComment)
                .forEach(deletedCommunityCommentRepository::save);
        communityPostRepository.delete(post);
    }

    @Transactional
    public void increaseHit(List<Long> postIdLists) {
        for (Long id : postIdLists) {
            val post = postRepository.findById(id)
                    .orElseThrow(() -> new NotFoundDBEntityException("Is not an exist post id"));
        }

        communityQueryRepository.updateHitsByPostId(postIdLists);
    }

    public void reportPost(Long memberId, Long postId) {
        val member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundDBEntityException("Is not a Member"));
        val post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundDBEntityException("Is not an exist post id"));
        reportPostRepository.save(ReportPost.builder()
                .reporterId(memberId)
                .postId(postId)
                .createdAt(LocalDateTime.now(KST))
                .build());
    }
}
