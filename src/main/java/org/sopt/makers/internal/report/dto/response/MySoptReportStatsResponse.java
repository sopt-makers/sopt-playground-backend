package org.sopt.makers.internal.report.dto.response;

import java.util.List;

public record MySoptReportStatsResponse(
	String myType,
	Integer totalDurationTime,
	Integer totalVisitCount,
	CommunityStatsDto myCommunityStats,
	ProfileStatsDto myProfileStats,
	GroupStatsDto myGroupStats,
	WordChainGameStatsDto myWordChainGameStats
) {
	public record CommunityStatsDto(
		Integer likeCount
	) {}

	public record ProfileStatsDto(
		Integer viewCount
	) {}

	public record GroupStatsDto(
		List<String> topFastestJoinedGroupList
	) {}

	public record WordChainGameStatsDto(
		Integer playCount,
		Integer winCount,
		List<String> wordList
	) {}
}
