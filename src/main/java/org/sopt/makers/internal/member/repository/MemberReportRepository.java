package org.sopt.makers.internal.member.repository;

import org.sopt.makers.internal.member.domain.MemberReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberReportRepository extends JpaRepository<MemberReport, Long> {
}
