package org.sopt.makers.internal.community.controller.comment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.dto.comment.CommentInfo;
import org.sopt.makers.internal.community.dto.request.comment.CommentSaveRequest;
import org.sopt.makers.internal.community.dto.response.CommentResponse;
import org.sopt.makers.internal.community.mapper.CommunityResponseMapper;
import org.sopt.makers.internal.community.service.comment.CommunityCommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/community/{postId}/comment")
@SecurityRequirement(name = "Authorization")
@Tag(name = "Community Comment 관련 API", description = "커뮤니티 댓글 관련 API")
public class CommunityCommentController {

    private final CommunityCommentService communityCommentService;
    private final CommunityResponseMapper communityResponseMapper;

    @Operation(
            summary = "댓글/답글 생성 API",
            description = """
              댓글 또는 답글을 생성합니다.
              - parentCommentId가 null: 댓글
              - parentCommentId가 있음: 답글

              멘션:
              - 일반 사용자 멘션: mention.userIds에 포함
              - 익명 사용자 멘션: anonymousMentionRequest.anonymousNickname에 닉네임만 포함
              """
    )
    @PostMapping
    public ResponseEntity<Map<String, Boolean>> createComment(
            @PathVariable("postId") Long postId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @RequestBody @Valid CommentSaveRequest request
    ) {
        request.validate();
        communityCommentService.createComment(userId, postId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("댓글 생성 성공", true));
    }

    @Operation(summary = "커뮤니티 댓글 조회 API")
    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @PathVariable("postId") Long postId,
            @RequestParam(value = "isBlockOn", required = false, defaultValue = "true") Boolean isBlockOn
    ) {
        List<CommentInfo> comments = communityCommentService.getPostCommentList(postId, userId, isBlockOn);
        List<CommentResponse> flatComments = comments.stream()
                .map(comment -> communityResponseMapper.toCommentResponse(comment, userId))
                .toList();
        List<CommentResponse> hierarchicalComments = communityResponseMapper.buildCommentHierarchy(flatComments);
        return ResponseEntity.status(HttpStatus.OK).body(hierarchicalComments);
    }

    @Operation(summary = "커뮤니티 댓글 삭제 API")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Map<String, Boolean>> deleteComment(
            @PathVariable("commentId") Long commentId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    ) {
        communityCommentService.deleteComment(commentId, userId);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("댓글 삭제 성공", true));
    }

    @Operation(summary = "커뮤니티 댓글 신고 API")
    @PostMapping("/{commentId}/report")
    public ResponseEntity<Map<String, Boolean>> reportComment(
            @PathVariable("commentId") Long commentId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    ) {
        communityCommentService.reportComment(userId, commentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("커뮤니티 댓글 신고 성공", true));
    }
}
