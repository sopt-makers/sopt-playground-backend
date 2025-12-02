package org.sopt.makers.internal.community.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.common.util.InfiniteScrollUtil;
import org.sopt.makers.internal.community.dto.CommunityPostMemberVo;
import org.sopt.makers.internal.community.dto.comment.CommentInfo;
import org.sopt.makers.internal.community.dto.request.CommunityHitRequest;
import org.sopt.makers.internal.community.dto.request.PostSaveRequest;
import org.sopt.makers.internal.community.dto.request.PostUpdateRequest;
import org.sopt.makers.internal.community.dto.response.*;
import org.sopt.makers.internal.community.service.post.CommunityPostService;
import org.sopt.makers.internal.community.mapper.CommunityResponseMapper;
import org.sopt.makers.internal.community.service.comment.CommunityCommentService;
import org.sopt.makers.internal.community.service.comment.CommunityCommentLikeService;
import org.sopt.makers.internal.vote.dto.request.VoteSelectionRequest;
import org.sopt.makers.internal.vote.dto.response.VoteResponse;
import org.sopt.makers.internal.vote.service.VoteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
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
    private final CommunityCommentLikeService commentLikeService;
    private final CommunityResponseMapper communityResponseMapper;
    private final InfiniteScrollUtil infiniteScrollUtil;

    private static final Long MEETING_CATEGORY_ID = 24L;

    @Operation(summary = "커뮤니티 글 상세 조회")
    @GetMapping("/posts/{postId}")
    public ResponseEntity<PostDetailResponse> getOnePost(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @PathVariable("postId") Long postId,
            @RequestParam(value = "isBlockOn", required = false, defaultValue = "true") Boolean isBlockOn
    ) {
        val postDetailData = communityPostService.getPostById(userId, postId, isBlockOn);
        val isLiked = communityPostService.isLiked(userId, postId);
        val likes = communityPostService.getLikes(postId);
        val anonymousProfile = communityPostService.getAnonymousPostProfile(postId);
        val response = communityResponseMapper.toPostDetailReponse(postDetailData, userId, isLiked, likes,
                anonymousProfile);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
            summary = "커뮤니티 글 전체 조회",
            description = """
                        categoryId: 카테고리 전체조회시 id값, 전체일 경우 null
                        cursor: 처음 조회시 null, 이외에 마지막 글 id
            """
    )
    @GetMapping("/posts")
    public ResponseEntity<PostAllResponse> getAllPosts(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @RequestParam(required = false, name = "categoryId") Long categoryId,
            @RequestParam(value = "isBlockOn", required = false, defaultValue = "true") Boolean isBlockOn,
            @RequestParam(required = false, name = "limit") Integer limit,
            @RequestParam(required = false, name = "cursor") Long cursor,
            @RequestParam(required = false, name = "page", defaultValue = "1") Integer page
    ) {
        if(Objects.equals(categoryId, MEETING_CATEGORY_ID)){
            PostAllResponse response = communityPostService.getMeetingPosts(userId, page, limit);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }

        List<CommunityPostMemberVo> posts = communityPostService.getAllPosts(categoryId, isBlockOn, userId,
                infiniteScrollUtil.checkLimitForPagination(limit), cursor);
        Boolean hasNextPosts = infiniteScrollUtil.checkHasNextElement(limit, posts);
        Map<Long, List<CommentInfo>> postCommentsMap = posts.stream()
                .collect(Collectors.toMap(
                        post -> post.post().id(),
                        post -> communityCommentService.getPostCommentList(post.post().id(), userId, isBlockOn)
                ));
        List<Long> allCommentIds = postCommentsMap.values().stream()
                .flatMap(List::stream)
                .map(c -> c.commentDao().comment().getId())
                .collect(Collectors.toList());

        Map<Long, Boolean> commentLikedMap = commentLikeService.getLikedMapByCommentIds(userId, allCommentIds);
        Map<Long, Integer> commentLikeCountMap = commentLikeService.getLikeCountMapByCommentIds(allCommentIds);

        val postResponse = posts.stream().map(post -> {
            List<CommentInfo> comments = postCommentsMap.get(post.post().id());
            val anonymousPostProfile = communityPostService.getAnonymousPostProfile(post.post().id());
            val isLiked = communityPostService.isLiked(userId, post.post().id());
            val likes = communityPostService.getLikes(post.post().id());

            return communityResponseMapper.toPostResponse(post, comments, userId, anonymousPostProfile,
                    isLiked, likes, commentLikedMap, commentLikeCountMap);
        }).collect(Collectors.toList());

        val response = new PostAllResponse(categoryId, hasNextPosts, postResponse);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "커뮤니티 글 조회수 증가")
    @PostMapping("/posts/hit")
    public ResponseEntity<Map<String, Boolean>> upPostHit(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @RequestBody CommunityHitRequest request
    ) {
        communityPostService.increaseHit(userId, request.postIdList());

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("success", true));
    }

    @Operation(summary = "커뮤니티 글 생성")
    @PostMapping("/posts")
    public ResponseEntity<PostSaveResponse> createPost(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @RequestBody @Valid PostSaveRequest request
    ) {

        return ResponseEntity.status(HttpStatus.CREATED).body(
                communityPostService.createPost(userId, request)
        );
    }

    @Operation(summary = "커뮤니티 글 수정")
    @PutMapping("/posts")
    public ResponseEntity<PostUpdateResponse> updatePost(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @RequestBody PostUpdateRequest request
    ) {
        val response = communityPostService.updatePost(userId, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @Operation(summary = "커뮤니티 글 신고 API")
    @PostMapping("/posts/{postId}/report")
    public ResponseEntity<Map<String, Boolean>> reportPost(
            @PathVariable("postId") Long postId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    ) {
        communityPostService.reportPost(userId, postId);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("커뮤니티 글 신고 성공", true));
    }

    @Operation(summary = "커뮤니티 글 삭제")
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Map<String, Boolean>> deletePost(
            @PathVariable("postId") Long postId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    ) {
        communityPostService.deletePost(postId, userId);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("커뮤니티 글 삭제 성공", true));
    }

    @Operation(summary = "커뮤니티 댓글 삭제 API")
    @DeleteMapping("/comment/{commentId}")
    public ResponseEntity<Map<String, Boolean>> deleteComment(
            @PathVariable("commentId") Long commentId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    ) {
        communityCommentService.deleteComment(commentId, userId);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("댓글 삭제 성공", true));
    }

    @Operation(summary = "커뮤니티 게시글 좋아요 API")
    @PostMapping("/posts/like/{postId}")
    public ResponseEntity<Map<String, Boolean>> likePost(
            @PathVariable("postId") Long postId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    ) {
        communityPostService.likePost(userId, postId);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("커뮤니티 게시글 좋아요 성공", true));
    }

    @Operation(summary = "커뮤니티 게시글 좋아요 취소 API")
    @DeleteMapping("/posts/unlike/{postId}")
    public ResponseEntity<Map<String, Boolean>> unlikePost(
            @PathVariable("postId") Long postId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    ) {
        communityPostService.unlikePost(userId, postId);
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
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    ) {
        List<RecentPostResponse> recentPosts = communityPostService.getRecentPosts(userId);
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
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @PathVariable Long postId,
            @RequestBody VoteSelectionRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(voteService.selectVote(postId, userId, request.selectedOptions()));
    }
}
