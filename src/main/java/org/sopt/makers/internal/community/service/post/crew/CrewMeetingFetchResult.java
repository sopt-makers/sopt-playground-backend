package org.sopt.makers.internal.community.service.post.crew;

import java.util.List;

public record CrewMeetingFetchResult(
	List<CrewMeetingCandidate> candidates,
	boolean hasMore
) {
}