package org.sopt.makers.internal.community.service.post.crew;

import java.util.List;

public record CrewMeetingFeedCache(
	List<CachedCrewPost> posts,
	boolean hasMorePage
) {
	public static CrewMeetingFeedCache empty() {
		return new CrewMeetingFeedCache(List.of(), true);
	}

	public List<CachedCrewPost> safePosts() {
		return posts == null ? List.of() : posts;
	}
}