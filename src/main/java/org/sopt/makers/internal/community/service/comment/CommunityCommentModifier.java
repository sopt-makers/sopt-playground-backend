package org.sopt.makers.internal.community.service.comment;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.domain.comment.CommunityComment;
import org.sopt.makers.internal.community.dto.request.CommentSaveRequest;
import org.sopt.makers.internal.community.repository.comment.CommunityCommentRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommunityCommentModifier {

    private final CommunityCommentRepository communityCommentRepository;

    // CREATE
    public CommunityComment createCommunityComment(Long postId, Long memberId, CommentSaveRequest request) {
        return communityCommentRepository.save(CommunityComment.builder()
                .content(request.content())
                .postId(postId)
                .writerId(memberId)
                .isBlindWriter(request.isBlindWriter())
                .build());
    }
}