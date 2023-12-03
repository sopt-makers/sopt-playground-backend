package org.sopt.makers.internal.repository.community;

import org.sopt.makers.internal.domain.community.DeletedCommunityPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeletedCommunityPostRepository extends JpaRepository<DeletedCommunityPost, Long> {
    Optional<DeletedCommunityPost> findById(Long id);
}
