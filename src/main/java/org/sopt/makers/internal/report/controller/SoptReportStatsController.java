package org.sopt.makers.internal.report.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.report.domain.SoptReportCategory;
import org.sopt.makers.internal.report.dto.response.MySoptReportStatsResponse;
import org.sopt.makers.internal.report.service.SoptReportStatsService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/report/stats")
@RequiredArgsConstructor
@Tag(name = "솝트 리포트 API")
public class SoptReportStatsController {
	private final SoptReportStatsService soptReportStatsService;

	@GetMapping
	public Map<String, Object> getReportStats(
		@RequestParam(required = false, defaultValue = "SOPT") SoptReportCategory category
	) {
		return soptReportStatsService.getSoptReportStats(category);
	}

	@GetMapping("/me")
	public MySoptReportStatsResponse getMySoptReportStats(
		@Parameter(hidden = true) @AuthenticationPrincipal Long userId
	) {
		return soptReportStatsService.getMySoptReportStats(userId);
	}

}
