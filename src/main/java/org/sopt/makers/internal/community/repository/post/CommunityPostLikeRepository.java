package org.sopt.makers.internal.community.repository.post;

import org.sopt.makers.internal.community.domain.CommunityPostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface CommunityPostLikeRepository extends JpaRepository<CommunityPostLike, Long> {

    // CREATE

    // READ
    Boolean existsByMemberIdAndPostId(Long memberId, Long postId);

    Optional<CommunityPostLike> findCommunityPostLikeByMemberIdAndPostId(Long memberId, Long postId);

    Integer countAllByPostId(Long postId);

    Integer countAllByMemberIdAndCreatedAtBetween(Long memberId, LocalDateTime start, LocalDateTime end);

    @Query("""
        SELECT postLike.post.id
        FROM CommunityPostLike postLike
        WHERE postLike.member.id = :memberId
          AND postLike.post.id IN :postIds
    """)
	List<Long> findLikedPostIdsByMemberIdAndPostIds(
        @Param("memberId") Long memberId,
        @Param("postIds") List<Long> postIds
    );

    @Query("""
        SELECT postLike.post.id AS postId,
               COUNT(postLike) AS likeCount
        FROM CommunityPostLike postLike
        WHERE postLike.post.id IN :postIds
        GROUP BY postLike.post.id
    """)
    List<PostLikeCountProjection> countLikesByPostIds(
        @Param("postIds") List<Long> postIds
    );

    interface PostLikeCountProjection {
        Long getPostId();
        Long getLikeCount();
    }
    // UPDATE

    // DELETE
}
