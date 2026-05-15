package org.sopt.makers.internal.community.dto;

import java.time.LocalDateTime;

public record CommunityDbCursor(
	LocalDateTime createdAt,
	Long postId
) {
}