package org.sopt.makers.internal.community.service.comment;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.domain.comment.CommunityComment;
import org.sopt.makers.internal.community.repository.comment.CommunityCommentRepository;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommunityCommentsRetriever {

    private final CommunityCommentRepository communityCommentRepository;

    public CommunityComment findCommunityCommentById(Long commentId) {
        return communityCommentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundDBEntityException("존재하지 않는 댓글의 id값 입니다."));
    }

    public void checkExistsCommunityCommentById(Long commentId) {
        if (!communityCommentRepository.existsById(commentId)) {
            throw new NotFoundDBEntityException("존재하지 않는 댓글의 id값 입니다.");
        }
    }
}
