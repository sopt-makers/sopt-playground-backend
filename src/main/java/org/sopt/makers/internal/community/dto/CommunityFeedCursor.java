package org.sopt.makers.internal.community.dto;

import java.time.LocalDateTime;

public record CommunityFeedCursor(
	LocalDateTime snapshotTime,
	CommunityDbCursor community,
	Integer meetingConsumedCount
) {

	public static CommunityFeedCursor initial(LocalDateTime snapshotTime) {
		return new CommunityFeedCursor(snapshotTime, null, 0);
	}

	public int safeMeetingConsumedCount() {
		return meetingConsumedCount == null ? 0 : meetingConsumedCount;
	}
}