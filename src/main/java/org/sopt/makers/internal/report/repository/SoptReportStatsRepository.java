package org.sopt.makers.internal.report.repository;

import java.util.List;

import org.sopt.makers.internal.report.domain.SoptReportStats;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SoptReportStatsRepository extends JpaRepository<SoptReportStats, Long> {

	List<SoptReportStats> findByCategory(String category);
}
