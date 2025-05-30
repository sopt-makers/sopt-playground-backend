package org.sopt.makers.internal.community.repository.comment;

import org.sopt.makers.internal.community.domain.comment.DeletedCommunityComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeletedCommunityCommentRepository extends JpaRepository<DeletedCommunityComment, Long> {
    List<DeletedCommunityComment> findAllByPostId(Long postId);
}
