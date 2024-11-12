package org.sopt.makers.internal.repository.community;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.sopt.makers.internal.community.repository.CommunityPostRepositoryCustom;
import org.sopt.makers.internal.domain.community.CommunityPost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long>, CommunityPostRepositoryCustom {
    Optional<CommunityPost> findById(Long id);

    List<CommunityPost> findAllByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
