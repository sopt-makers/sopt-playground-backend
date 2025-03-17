package org.sopt.makers.internal.repository.community;

import org.sopt.makers.internal.domain.community.CommunityComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CommunityCommentRepository extends JpaRepository<CommunityComment, Long> {
    List<CommunityComment> findAllByPostId(Long postId);

    int countAllByPostId(Long postId);

    int countAllByWriterIdAndCreatedAtBetween(Long memberId, LocalDateTime start, LocalDateTime end);
}
