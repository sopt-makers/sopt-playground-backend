package org.sopt.makers.internal.repository.community;

import org.sopt.makers.internal.domain.community.DeletedCommunityComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeletedCommunityCommentRepository extends JpaRepository<DeletedCommunityComment, Long> {
    List<DeletedCommunityComment> findAllByPostId(Long postId);
}
