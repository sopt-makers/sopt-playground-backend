package org.sopt.makers.internal.report.dto;

import java.util.List;

public record CrewFastestJoinedGroupResponse(
	List<CrewGroupDto> topFastestAppliedMeetings
) {
	public record CrewGroupDto(
		Long meetingId,
		String title
	) { }
}
