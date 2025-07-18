package org.sopt.makers.internal.community.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.common.util.InfiniteScrollUtil;
import org.sopt.makers.internal.community.dto.CommunityPostMemberVo;
import org.sopt.makers.internal.community.dto.request.CommentSaveRequest;
import org.sopt.makers.internal.community.dto.request.CommunityHitRequest;
import org.sopt.makers.internal.community.dto.request.PostSaveRequest;
import org.sopt.makers.internal.community.dto.request.PostUpdateRequest;
import org.sopt.makers.internal.community.dto.response.*;
import org.sopt.makers.internal.community.service.post.CommunityPostService;
import org.sopt.makers.internal.internal.InternalMemberDetails;
import org.sopt.makers.internal.community.mapper.CommunityResponseMapper;
import org.sopt.makers.internal.community.service.CommunityCommentService;
import org.sopt.makers.internal.vote.dto.request.VoteSelectionRequest;
import org.sopt.makers.internal.vote.dto.response.VoteResponse;
import org.sopt.makers.internal.vote.service.VoteService;
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
    private final VoteService voteService;
    private final CommunityCommentService communityCommentService;
    private final CommunityResponseMapper communityResponseMapper;
    private final InfiniteScrollUtil infiniteScrollUtil;

    @Operation(summary = "커뮤니티 글 상세 조회")
    @GetMapping("/posts/{postId}")
    public ResponseEntity<PostDetailResponse> getOnePost(
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails,
            @PathVariable("postId") Long postId,
            @RequestParam(value = "isBlockOn", required = false, defaultValue = "true") Boolean isBlockOn
    ) {
        val post = communityPostService.getPostById(memberDetails.getId(), postId, isBlockOn);
        val isLiked = communityPostService.isLiked(memberDetails.getId(), post.post().id());
        val likes = communityPostService.getLikes(post.post().id());
        val anonymousProfile = communityPostService.getAnonymousPostProfile(post.post().id());
        val response = communityResponseMapper.toPostDetailReponse(post, memberDetails.getId(), isLiked, likes,
                anonymousProfile);
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
            @RequestParam(value = "isBlockOn", required = false, defaultValue = "true") Boolean isBlockOn,
            @RequestParam(required = false, name = "limit") Integer limit,
            @RequestParam(required = false, name = "cursor") Long cursor
    ) {
        List<CommunityPostMemberVo> posts = communityPostService.getAllPosts(categoryId, isBlockOn, memberDetails.getId(),
                infiniteScrollUtil.checkLimitForPagination(limit), cursor);
        val hasNextPosts = infiniteScrollUtil.checkHasNextElement(limit, posts);
        val postResponse = posts.stream().map(post -> {
            val comments = communityCommentService.getPostCommentList(post.post().id(), memberDetails.getId(),
                    isBlockOn);
            val anonymousPostProfile = communityPostService.getAnonymousPostProfile(post.post().id());
            val isLiked = communityPostService.isLiked(memberDetails.getId(), post.post().id());
            val likes = communityPostService.getLikes(post.post().id());
            return communityResponseMapper.toPostResponse(post, comments, memberDetails.getId(), anonymousPostProfile,
                    isLiked, likes);
        }).collect(Collectors.toList());
        val response = new PostAllResponse(categoryId, hasNextPosts, postResponse);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "커뮤니티 글 조회수 증가")
    @PostMapping("/posts/hit")
    public ResponseEntity<Map<String, Boolean>> upPostHit(
            @RequestBody CommunityHitRequest request
    ) {
        communityPostService.increaseHit(request.postIdList());

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("success", true));
    }

    @Operation(summary = "커뮤니티 글 생성")
    @PostMapping("/posts")
    public ResponseEntity<PostSaveResponse> createPost(
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails,
            @RequestBody @Valid PostSaveRequest request
    ) {

        return ResponseEntity.status(HttpStatus.CREATED).body(
                communityPostService.createPost(memberDetails.getId(), request)
        );
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
            @PathVariable("postId") Long postId,
            @RequestParam(value = "isBlockOn", required = false, defaultValue = "true") Boolean isBlockOn
    ) {
        val comments = communityCommentService.getPostCommentList(postId, memberDetails.getId(), isBlockOn);
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

    @Operation(summary = "커뮤니티 홈 인기글 조회 API")
    @GetMapping("/posts/popular")
    public ResponseEntity<List<PopularPostResponse>> getPopularPost(
            @Parameter(description = "조회할 인기글 개수 (기본값: 3)")
            @RequestParam(defaultValue = "3") int limit) {
        return ResponseEntity.ok(communityPostService.getPopularPosts(limit));
    }

    @Operation(summary = "커뮤니티 홈 최근 솝티클 목록 조회 API")
    @GetMapping("/posts/sopticle")
    public ResponseEntity<List<SopticlePostResponse>> getRecentSopticlePost() {
        List<SopticlePostResponse> sopticlePosts = communityPostService.getRecentSopticlePosts();
        return ResponseEntity.ok().body(sopticlePosts);
    }

    @Operation(summary = "커뮤니티 홈 모든 카테고리 최신글 조회 API")
    @GetMapping("/posts/all/recent")
    public ResponseEntity<List<RecentPostResponse>> getRecentPosts(
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
    ) {
        List<RecentPostResponse> recentPosts = communityPostService.getRecentPosts(memberDetails.getId());
        return ResponseEntity.ok().body(recentPosts);
    }

    @Deprecated
    @Operation(summary = "핫 게시물 조회 API")
    @GetMapping("/posts/hot")
    public ResponseEntity<Object> getTodayHotPost() {
        val todayPosts = communityPostService.getTodayPosts();
        val todayHotPost = communityPostService.findTodayHotPost(todayPosts);
        if (todayHotPost == null) {
            val recentHotPost = communityPostService.getRecentHotPost();
            return ResponseEntity.status(HttpStatus.OK).body(HotPostResponse.of(recentHotPost));
        }
        return ResponseEntity.status(HttpStatus.OK).body(HotPostResponse.of(todayHotPost));
    }

    @Operation(summary = "투표 선택 API")
    @PostMapping("/posts/{postId}/vote")
    public ResponseEntity<VoteResponse> vote(
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails,
            @PathVariable Long postId,
            @RequestBody VoteSelectionRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(voteService.selectVote(postId, memberDetails.getId(), request.selectedOptions()));
    }
}
