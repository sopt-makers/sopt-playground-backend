package org.sopt.makers.internal.repository;

import org.sopt.makers.internal.domain.CommunityComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityCommentRepository extends JpaRepository<CommunityComment, Long> {
}
