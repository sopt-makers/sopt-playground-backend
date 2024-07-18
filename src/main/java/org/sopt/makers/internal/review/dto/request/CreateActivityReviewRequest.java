package org.sopt.makers.internal.review.dto.request;

import javax.validation.constraints.NotBlank;

public record CreateActivityReviewRequest(
	@NotBlank String content
) { }
