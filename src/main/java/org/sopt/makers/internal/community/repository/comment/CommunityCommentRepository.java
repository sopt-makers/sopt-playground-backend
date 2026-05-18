package org.sopt.makers.internal.community.repository.comment;

import org.sopt.makers.internal.community.domain.comment.CommunityComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;


public interface CommunityCommentRepository extends JpaRepository<CommunityComment, Long> {
    List<CommunityComment> findAllByPostId(Long postId);

    List<CommunityComment> findAllByParentCommentId(Long parentCommentId);

    int countAllByPostId(Long postId);

    int countAllByPostIdAndIsDeleted(Long postId, Boolean isDeleted);

    int countAllByWriterIdAndCreatedAtBetween(Long memberId, LocalDateTime start, LocalDateTime end);

    @Query("""
        SELECT comment.postId AS postId,
               COUNT(comment) AS commentCount
        FROM CommunityComment comment
        WHERE comment.postId IN :postIds
          AND comment.isDeleted = false
        GROUP BY comment.postId
    """)
    List<PostCommentCountProjection> countCommentsByPostIds(
        @Param("postIds") List<Long> postIds
    );

    interface PostCommentCountProjection {
        Long getPostId();
        Long getCommentCount();
    }
}
