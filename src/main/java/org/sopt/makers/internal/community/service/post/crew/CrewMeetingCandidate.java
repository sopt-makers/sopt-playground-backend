package org.sopt.makers.internal.community.service.post.crew;

import java.time.LocalDateTime;
import org.sopt.makers.internal.community.dto.response.PostResponse;

public record CrewMeetingCandidate(
	LocalDateTime createdAt,
	Integer nextConsumedCount,
	PostResponse response
) {
}