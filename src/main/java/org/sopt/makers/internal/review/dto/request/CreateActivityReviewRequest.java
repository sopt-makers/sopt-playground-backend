package org.sopt.makers.internal.review.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateActivityReviewRequest(
	@NotBlank String content
) { }
