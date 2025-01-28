package org.sopt.makers.internal.report.controller;

import java.util.Map;

import org.sopt.makers.internal.domain.InternalMemberDetails;
import org.sopt.makers.internal.report.dto.response.MySoptReportStatsResponse;
import org.sopt.makers.internal.report.service.SoptReportStatsService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/report/stats")
@RequiredArgsConstructor
public class SoptReportStatsController {
	private final SoptReportStatsService soptReportStatsService;

	@GetMapping
	public Map<String, Object> getReportStats(
		@RequestParam(required = false, defaultValue = "SOPT") String category
	) {
		return soptReportStatsService.getSoptReportStats(category);
	}

	@GetMapping("/me")
	public MySoptReportStatsResponse getMySoptReportStats(
		@Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
	) {
		return soptReportStatsService.getMySoptReportStats(memberDetails.getId());
	}

}
