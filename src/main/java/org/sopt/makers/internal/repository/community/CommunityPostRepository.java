package org.sopt.makers.internal.repository.community;

import org.sopt.makers.internal.domain.community.CommunityPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {
    Optional<CommunityPost> findById(Long id);

    List<CommunityPost> findAllByCreatedAt(LocalDate date);
}
