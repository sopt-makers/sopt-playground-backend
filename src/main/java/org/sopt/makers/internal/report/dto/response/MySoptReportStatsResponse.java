package org.sopt.makers.internal.report.dto.response;

import java.util.List;

public record MySoptReportStatsResponse(
	String myType,
	Integer totalDurationTime,
	Integer totalVisitCount,
	CommunityStatsDto myCommunityStats,
	ProfileStatsDto myProfileStats,
	CrewStatsDto myCrewStats,
	WordChainGameStatsDto myWordChainGameStats
) {
	public record CommunityStatsDto(
		Integer likeCount
	) {}

	public record ProfileStatsDto(
		Integer viewCount
	) {}

	public record CrewStatsDto(
		List<String> topFastestJoinedGroupList
	) {}

	public record WordChainGameStatsDto(
		Integer playCount,
		Integer winCount,
		List<String> wordList
	) {}
}
