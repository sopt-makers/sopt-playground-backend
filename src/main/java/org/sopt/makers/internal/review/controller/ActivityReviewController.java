package org.sopt.makers.internal.review.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.review.dto.request.CreateActivityReviewRequest;
import org.sopt.makers.internal.review.dto.response.PagedActivityReviewResponse;
import org.sopt.makers.internal.review.service.ActivityReviewService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/review")
@SecurityRequirement(name = "Authorization")
@Tag(name = "활동 후기 관련 API", description = "활동 후기 관련 API List")
public class ActivityReviewController {

	private final ActivityReviewService activityReviewService;

	@Operation(summary = "활동 후기 생성")
	@PostMapping
	public ResponseEntity<Map<String, Boolean>> createActivityReview(
		@Parameter(hidden = true) @AuthenticationPrincipal Long userId,
		@Valid @RequestBody CreateActivityReviewRequest request
	) {
		activityReviewService.createActivityReview(request, userId);
		return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("활동 후기 생성 성공", true));
	}

	@Operation(summary = "활동 후기 조회")
	@GetMapping
	public ResponseEntity<PagedActivityReviewResponse> getActivityReviews(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size
	) {
		PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
		return ResponseEntity.status(HttpStatus.OK).body(
				activityReviewService.getActivityReviews(pageRequest)
		);
	}
}
