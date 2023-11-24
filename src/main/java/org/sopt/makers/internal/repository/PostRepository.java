package org.sopt.makers.internal.repository;

import org.sopt.makers.internal.domain.community.CommunityPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<CommunityPost, Long> {
    Optional<CommunityPost> findById(Long id);
}
