package org.sopt.makers.internal.repository.member;

import org.sopt.makers.internal.domain.member.MemberReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberReportRepository extends JpaRepository<MemberReport, Long> {
}
