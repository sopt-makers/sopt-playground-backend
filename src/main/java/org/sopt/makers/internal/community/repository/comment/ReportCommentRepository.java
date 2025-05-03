package org.sopt.makers.internal.community.repository.comment;

import org.sopt.makers.internal.community.domain.comment.ReportComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportCommentRepository extends JpaRepository<ReportComment, Long> {
}
