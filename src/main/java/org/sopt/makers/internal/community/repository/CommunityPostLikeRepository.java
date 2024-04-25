package org.sopt.makers.internal.community.repository;

import org.sopt.makers.internal.community.domain.CommunityPostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommunityPostLikeRepository extends JpaRepository<CommunityPostLike, Long> {

    // CREATE

    // READ
    Boolean existsByMemberIdAndPostId(Long memberId, Long postId);

    Optional<CommunityPostLike> findCommunityPostLikeByMemberIdAndPostId(Long memberId, Long postId);

    Integer countAllByPostId(Long postId);

    // UPDATE

    // DELETE
}
