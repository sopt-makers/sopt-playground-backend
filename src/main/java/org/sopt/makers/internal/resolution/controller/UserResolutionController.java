package org.sopt.makers.internal.resolution.controller;

import org.sopt.makers.internal.domain.InternalMemberDetails;
import org.sopt.makers.internal.resolution.dto.request.ResolutionSaveRequest;
import org.sopt.makers.internal.resolution.dto.response.ResolutionSaveResponse;
import org.sopt.makers.internal.resolution.service.UserResolutionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/resolution")
@SecurityRequirement(name = "Authorization")
@Tag(name = "다짐 메시지 관련 API", description = "다짐 메시지 관련 API List")
public class UserResolutionController {

	private final UserResolutionService userResolutionService;

	@Operation(summary = "다짐 메시지 생성")
	@PostMapping
	public ResponseEntity<ResolutionSaveResponse> createResolution(
		@Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails,
		@RequestBody ResolutionSaveRequest request
	) {
		val response = userResolutionService.createResolution(memberDetails.getId(), request);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
}
