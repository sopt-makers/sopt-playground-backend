package org.sopt.makers.internal.community.dto;

import java.time.LocalDateTime;

public record CommunityFeedCursor(
	LocalDateTime snapshotTime,
	CommunityDbCursor community,
	Integer meetingConsumedCount
) {
	public CommunityFeedCursor {
		if (snapshotTime == null) {
			throw new IllegalArgumentException("snapshot time must not be null");
		}
		if (meetingConsumedCount != null && meetingConsumedCount < 0) {
			throw new IllegalArgumentException("meeting consumed count must not be negative");
		}
		if (community != null && (community.createdAt() == null || community.postId() == null)) {
			throw new IllegalArgumentException("community must include both createdAt and postId");
		}
	}

	public static CommunityFeedCursor initial(LocalDateTime snapshotTime) {
		return new CommunityFeedCursor(snapshotTime, null, 0);
	}

	public int safeMeetingConsumedCount() {
		return meetingConsumedCount == null ? 0 : meetingConsumedCount;
	}
}
