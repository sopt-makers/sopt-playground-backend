package org.sopt.makers.internal.community.repository.post;

import org.sopt.makers.internal.community.domain.CommunityPostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CommunityPostLikeRepository extends JpaRepository<CommunityPostLike, Long> {

    // CREATE

    // READ
    Boolean existsByMemberIdAndPostId(Long memberId, Long postId);

    Optional<CommunityPostLike> findCommunityPostLikeByMemberIdAndPostId(Long memberId, Long postId);

    Integer countAllByPostId(Long postId);

    Integer countAllByMemberIdAndCreatedAtBetween(Long memberId, LocalDateTime start, LocalDateTime end);

    // UPDATE

    // DELETE
}
