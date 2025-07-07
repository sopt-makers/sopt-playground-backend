package org.sopt.makers.internal.community.repository.post;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.sopt.makers.internal.community.domain.CommunityPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long>, CommunityPostRepositoryCustom {
    Optional<CommunityPost> findById(Long id);

    List<CommunityPost> findAllByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    Integer countAllByMemberIdAndCreatedAtBetween(Long memberId, LocalDateTime start, LocalDateTime end);

    List<CommunityPost> findTop5ByCategoryIdOrderByCreatedAtDesc(Long categoryId);

    List<CommunityPost> findTop5ByCategoryIdNotOrderByCreatedAtDesc(Long categoryId);

    @Modifying
    @Query("UPDATE CommunityPost p SET p.hits = p.hits + 1 WHERE p.id = :postId")
    void increaseHitsDirect(@Param("postId") Long postId);

    @Query(value =
            "SELECT COUNT(*) " +
            "FROM CommunityPost p " +
            "WHERE p.categoryId = 21 AND p.member.id = :memberId"
            )
    long countSopticleByMemberId(@Param("memberId") Long memberId);
}
