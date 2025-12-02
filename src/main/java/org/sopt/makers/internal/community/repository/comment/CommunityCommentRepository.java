package org.sopt.makers.internal.community.repository.comment;

import org.sopt.makers.internal.community.domain.comment.CommunityComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CommunityCommentRepository extends JpaRepository<CommunityComment, Long> {
    List<CommunityComment> findAllByPostId(Long postId);

    List<CommunityComment> findAllByParentCommentId(Long parentCommentId);

    int countAllByPostId(Long postId);

    int countAllByPostIdAndIsDeleted(Long postId, Boolean isDeleted);

    int countAllByWriterIdAndCreatedAtBetween(Long memberId, LocalDateTime start, LocalDateTime end);
}
