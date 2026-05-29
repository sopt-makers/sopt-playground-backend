package org.sopt.makers.internal.community.repository.post;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.sopt.makers.internal.community.domain.CommunityPost;
import org.sopt.makers.internal.community.domain.enums.CommunityCategoryCode;
import org.sopt.makers.internal.community.domain.enums.CommunityCategoryGroup;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long>, CommunityPostRepositoryCustom {

    Optional<CommunityPost> findById(Long id);

    @EntityGraph(attributePaths = {"category", "category.parent", "member"})
    @Query("""
        SELECT post
        FROM CommunityPost post
        WHERE post.id = :postId
    """)
    Optional<CommunityPost> findByIdWithCategoryAndMember(@Param("postId") Long postId);

    List<CommunityPost> findAllByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    Integer countAllByMemberIdAndCreatedAtBetween(Long memberId, LocalDateTime start, LocalDateTime end);

    @EntityGraph(attributePaths = {"category", "member"})
    List<CommunityPost> findTop5ByCategory_CodeInOrderByCreatedAtDesc(
        List<CommunityCategoryCode> categoryCodes
    );

    @EntityGraph(attributePaths = {"category", "category.parent"})
    List<CommunityPost> findTop3ByCategory_CategoryGroupInOrderByCreatedAtDesc(
        List<CommunityCategoryGroup> categoryGroups
    );

    @EntityGraph(attributePaths = {"category", "category.parent"})
    Optional<CommunityPost> findFirstByCategory_CodeInOrderByCreatedAtDesc(
        List<CommunityCategoryCode> categoryCodes
    );

    @Modifying
    @Query("UPDATE CommunityPost post SET post.hits = post.hits + 1 WHERE post.id = :postId")
    void increaseHitsDirect(@Param("postId") Long postId);

    @Query("""
        SELECT COUNT(post)
        FROM CommunityPost post
        WHERE post.category.code IN :categoryCodes
          AND post.member.id = :memberId
    """)
    long countByMemberIdAndCategoryCodes(
        @Param("memberId") Long memberId,
        @Param("categoryCodes") List<CommunityCategoryCode> categoryCodes
    );

    @Query("""
        SELECT COUNT(post)
        FROM CommunityPost post
        WHERE post.member.id = :memberId
          AND post.category.categoryGroup = :categoryGroup
    """)
    long countByMemberIdAndCategoryGroup(
        @Param("memberId") Long memberId,
        @Param("categoryGroup") CommunityCategoryGroup categoryGroup
    );

    default long countSopticleByMemberId(Long memberId) {
        return countByMemberIdAndCategoryGroup(memberId, CommunityCategoryGroup.SOPTICLE);
    }
}