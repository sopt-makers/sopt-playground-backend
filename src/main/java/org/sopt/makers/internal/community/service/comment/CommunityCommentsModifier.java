package org.sopt.makers.internal.community.service.comment;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.domain.comment.CommunityComment;
import org.sopt.makers.internal.community.dto.request.comment.CommentSaveRequest;
import org.sopt.makers.internal.community.dto.request.comment.CommentUpdateRequest;
import org.sopt.makers.internal.community.repository.comment.CommunityCommentRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommunityCommentsModifier {

    private final CommunityCommentRepository communityCommentRepository;

    public CommunityComment createCommunityComment(Long postId, Long memberId, CommentSaveRequest request) {
        return communityCommentRepository.save(CommunityComment.builder()
                .content(request.content())
                .postId(postId)
                .writerId(memberId)
                .isBlindWriter(request.isBlindWriter())
                .parentCommentId(request.parentCommentId())
                .build());
    }

    public void updateCommunityComment(
            CommunityComment comment,
            CommentUpdateRequest request
    ) {
        comment.updateContent(request.content());
        communityCommentRepository.save(comment);
    }
}