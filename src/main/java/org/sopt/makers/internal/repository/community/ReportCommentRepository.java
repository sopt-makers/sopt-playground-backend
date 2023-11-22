package org.sopt.makers.internal.repository.community;

import org.sopt.makers.internal.domain.community.ReportComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportCommentRepository extends JpaRepository<ReportComment, Long> {
}
