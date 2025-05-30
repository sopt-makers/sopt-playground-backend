package org.sopt.makers.internal.resolution.controller;

import org.sopt.makers.internal.internal.InternalMemberDetails;
import org.sopt.makers.internal.resolution.dto.request.ResolutionSaveRequest;
import org.sopt.makers.internal.resolution.dto.response.ResolutionResponse;
import org.sopt.makers.internal.resolution.dto.response.ResolutionValidResponse;
import org.sopt.makers.internal.resolution.service.UserResolutionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/resolution")
@SecurityRequirement(name = "Authorization")
@Tag(name = "다짐 메시지 관련 API", description = "다짐 메세지 관련 API List")
public class UserResolutionController {

	private final UserResolutionService userResolutionService;

	@Operation(summary = "다짐 메세지 조회")
	@GetMapping
	public ResponseEntity<ResolutionResponse> getResolution(
			@Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
	) {
		return ResponseEntity.status(HttpStatus.OK).body(userResolutionService.getResolution(memberDetails.getId()));
	}

	@Operation(summary = "다짐 메세지 생성")
	@PostMapping
	public ResponseEntity<Void> createResolution(
		@Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails,
		@Valid @RequestBody ResolutionSaveRequest request
	) {
		userResolutionService.createResolution(memberDetails.getId(), request);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@Operation(summary = "다짐 메세지 유효성 검사")
	@GetMapping("/validation")
	public ResponseEntity<ResolutionValidResponse> validation(
		@Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
	) {
		return ResponseEntity.status(HttpStatus.OK).body(userResolutionService.validation(memberDetails.getId()));
	}

	@Operation(summary = "다짐 메세지 삭제")
	@DeleteMapping
	public ResponseEntity<Void> deleteResolution(
			@Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
	){
		userResolutionService.deleteResolution(memberDetails.getId());
		return ResponseEntity.noContent().build();
	}
}
