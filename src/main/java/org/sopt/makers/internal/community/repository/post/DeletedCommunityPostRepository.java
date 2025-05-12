package org.sopt.makers.internal.community.repository.post;

import org.sopt.makers.internal.community.domain.DeletedCommunityPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeletedCommunityPostRepository extends JpaRepository<DeletedCommunityPost, Long> {
    Optional<DeletedCommunityPost> findById(Long id);
}
