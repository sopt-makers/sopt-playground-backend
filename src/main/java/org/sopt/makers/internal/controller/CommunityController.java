package org.sopt.makers.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.domain.InternalMemberDetails;
import org.sopt.makers.internal.dto.community.CommentListResponse;
import org.sopt.makers.internal.dto.community.CommentSaveRequest;
import org.sopt.makers.internal.service.CommunityCommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/community")
@SecurityRequirement(name = "Authorization")
@Tag(name = "Community 관련 API", description = "Community 관련 API List")
public class CommunityController {
    private final CommunityCommentService communityCommentService;

    @Operation(summary = "커뮤니티 댓글 생성 API")
    @PostMapping("/{postId}/comment")
    public ResponseEntity<Map<String, Boolean>> createComment(
            @PathVariable("postId") Long postId,
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails,
            @RequestBody @Valid CommentSaveRequest request
    ) {
        val writerId = memberDetails.getId();
        communityCommentService.createComment(writerId, postId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("success", true));
    }

    @Operation(summary = "커뮤니티 댓글 조회 API")
    @GetMapping("/{postId}/comment")
    public ResponseEntity<List<CommentListResponse>> getComments(@PathVariable("postId") Long postId) {
        return ResponseEntity.status(HttpStatus.OK).body(communityCommentService.getCommentList(postId));
    }

    @Operation(summary = "커뮤니티 댓글 삭제 API")
    @DeleteMapping("/comment/{commentId}")
    public ResponseEntity<Map<String, Boolean>> deleteComment(
            @PathVariable("commentId") Long commentId,
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
    ) {
        val writerId = memberDetails.getId();
        communityCommentService.deleteComment(commentId, writerId);

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("success", true));
    }
}