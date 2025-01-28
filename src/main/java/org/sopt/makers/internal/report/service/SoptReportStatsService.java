package org.sopt.makers.internal.report.service;

import static org.sopt.makers.internal.common.JsonDataSerializer.*;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.sopt.makers.internal.report.domain.SoptReportStats;
import org.sopt.makers.internal.report.repository.SoptReportStatsRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SoptReportStatsService {
	private final SoptReportStatsRepository soptReportStatsRepository;

	public Map<String, Object> getSoptReportStats(String category) {
		return soptReportStatsRepository.findByCategory(category).stream().collect(
			Collectors.toMap(
				SoptReportStats::getTemplateKey,
				stats -> Objects.requireNonNull(serialize(stats.getData()))
			)
		);
	}
}
