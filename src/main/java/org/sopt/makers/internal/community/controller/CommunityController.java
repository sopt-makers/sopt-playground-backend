package org.sopt.makers.internal.community.controller;

import io.github.bucket4j.Bucket;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.common.InfiniteScrollUtil;
import org.sopt.makers.internal.community.service.CommunityPostService;
import org.sopt.makers.internal.domain.InternalMemberDetails;
import org.sopt.makers.internal.dto.community.*;
import org.sopt.makers.internal.mapper.CommunityResponseMapper;
import org.sopt.makers.internal.service.CommunityCategoryService;
import org.sopt.makers.internal.community.service.CommunityCommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/community")
@SecurityRequirement(name = "Authorization")
@Tag(name = "Community 관련 API", description = "Community 관련 API List")
public class CommunityController {

    private final CommunityPostService communityPostService;
    private final CommunityCategoryService communityCategoryService;
    private final CommunityCommentService communityCommentService;
    private final CommunityResponseMapper communityResponseMapper;
    private final InfiniteScrollUtil infiniteScrollUtil;
    private final Bucket bucket;

    @Operation(summary = "커뮤니티 전체 카테고리 조회")
    @GetMapping("/category")
    public ResponseEntity<List<CategoryDto>> getCategoryList() {
        val response = communityCategoryService.getAllCategory();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "커뮤니티 글 상세 조회")
    @GetMapping("/posts/{postId}")
    public ResponseEntity<PostDetailResponse> getCategoryList(
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails,
            @PathVariable("postId") Long postId
    ) {
        val post = communityPostService.getPostById(postId);
        val isLiked = communityPostService.isLiked(memberDetails.getId(), post.post().id());
        val likes = communityPostService.getLikes(post.post().id());
        val anonymousProfile = communityPostService.getAnonymousPostProfile(post.member().id(), post.post().id());
        val response = communityResponseMapper.toPostDetailReponse(post, memberDetails.getId(), isLiked, likes, anonymousProfile);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
            summary = "커뮤니티 글 전체 조회",
            description =
                    """
                            categoryId: 카테고리 전체조회시 id값, 전체일 경우 null
                            
                            cursor: 처음 조회시 null, 이외에 마지막 글 id
                            """
    )
    @GetMapping("/posts")
    public ResponseEntity<PostAllResponse> getAllPosts(
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails,
            @RequestParam(required = false, name = "categoryId") Long categoryId,
            @RequestParam(required = false, name = "limit") Integer limit,
            @RequestParam(required = false, name = "cursor") Long cursor
    ) {
        val posts = communityPostService.getAllPosts(categoryId, infiniteScrollUtil.checkLimitForPagination(limit), cursor);
        val hasNextPosts = infiniteScrollUtil.checkHasNextElement(limit, posts);
        val postResponse = posts.stream().map(post -> {
            val comments = communityCommentService.getPostCommentList(post.post().id());
            val anonymousPostProfile = communityPostService.getAnonymousPostProfile(post.member().id(), post.post().id());
            val isLiked = communityPostService.isLiked(memberDetails.getId(), post.post().id());
            val likes = communityPostService.getLikes(post.post().id());
            return communityResponseMapper.toPostResponse(post, comments, memberDetails.getId(), anonymousPostProfile, isLiked, likes);
        }).collect(Collectors.toList());
        val response = new PostAllResponse(categoryId, hasNextPosts, postResponse);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "커뮤니티 글 조회수 증가")
    @PostMapping("/posts/hit")
    public ResponseEntity<Map<String, Boolean>> upPostHit(
            @RequestBody CommunityHitRequest request
    ) {
        if (bucket.tryConsume(1)) {
            communityPostService.increaseHit(request.postIdList());
        }

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("success", true));
    }

    @Operation(summary = "커뮤니티 글 생성")
    @PostMapping("/posts")
    public ResponseEntity<PostSaveResponse> createPost(
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails,
            @RequestBody PostSaveRequest request
    ) {
        val response = communityPostService.createPost(memberDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "커뮤니티 글 수정")
    @PutMapping("/posts")
    public ResponseEntity<PostUpdateResponse> updatePost(
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails,
            @RequestBody PostUpdateRequest request
    ) {
        val response = communityPostService.updatePost(memberDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @Operation(summary = "커뮤니티 글 신고 API")
    @PostMapping("/posts/{postId}/report")
    public ResponseEntity<Map<String, Boolean>> reportPost(
            @PathVariable("postId") Long postId,
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
    ) {
        communityPostService.reportPost(memberDetails.getId(), postId);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("커뮤니티 글 신고 성공", true));
    }

    @Operation(summary = "커뮤니티 글 삭제")
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Map<String, Boolean>> deletePost(
            @PathVariable("postId") Long postId,
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
    ) {
        communityPostService.deletePost(postId, memberDetails.getId());

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("커뮤니티 글 삭제 성공", true));
    }

    @Operation(summary = "커뮤니티 댓글 생성 API")
    @PostMapping("/{postId}/comment")
    public ResponseEntity<Map<String, Boolean>> createComment(
            @PathVariable("postId") Long postId,
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails,
            @RequestBody @Valid CommentSaveRequest request
    ) {
        val writerId = memberDetails.getId();
        communityCommentService.createComment(writerId, postId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("댓글 생성 성공", true));
    }

    @Operation(summary = "커뮤니티 댓글 조회 API")
    @GetMapping("/{postId}/comment")
    public ResponseEntity<List<CommentResponse>> getComments(
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails,
            @PathVariable("postId") Long postId
    ) {
        val comments = communityCommentService.getPostCommentList(postId);
        val response = comments.stream().
                map(comment -> communityResponseMapper.toCommentResponse(comment, memberDetails.getId(),
                    communityCommentService.getAnonymousCommentProfile(comment.comment()))).toList();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "커뮤니티 댓글 삭제 API")
    @DeleteMapping("/comment/{commentId}")
    public ResponseEntity<Map<String, Boolean>> deleteComment(
            @PathVariable("commentId") Long commentId,
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
    ) {
        val writerId = memberDetails.getId();
        communityCommentService.deleteComment(commentId, writerId);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("댓글 삭제 성공", true));
    }

    @Operation(summary = "커뮤니티 댓글 신고 API")
    @PostMapping("/comment/{commentId}/report")
    public ResponseEntity<Map<String, Boolean>> reportComment(
            @PathVariable("commentId") Long commentId,
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
    ) {
        communityCommentService.reportComment(memberDetails.getId(), commentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("커뮤니티 댓글 신고 성공", true));
    }

    @Operation(summary = "커뮤니티 게시글 좋아요 API")
    @PostMapping("/posts/like/{postId}")
    public ResponseEntity<Map<String, Boolean>> likePost(
            @PathVariable("postId") Long postId,
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
    ) {

        communityPostService.likePost(memberDetails.getId(), postId);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("커뮤니티 게시글 좋아요 성공", true));
    }

    @Operation(summary = "커뮤니티 게시글 좋아요 취소 API")
    @DeleteMapping("/posts/unlike/{postId}")
    public ResponseEntity<Map<String, Boolean>> unlikePost(
            @PathVariable("postId") Long postId,
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
    ) {

        communityPostService.unlikePost(memberDetails.getId(), postId);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("커뮤니티 게시글 좋아요 취소 성공", true));
    }
}
