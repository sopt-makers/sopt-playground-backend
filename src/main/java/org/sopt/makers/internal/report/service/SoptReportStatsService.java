package org.sopt.makers.internal.report.service;

import static org.sopt.makers.internal.common.Constant.*;
import static org.sopt.makers.internal.common.JsonDataSerializer.*;
import static org.sopt.makers.internal.config.cache.CacheConstant.*;
import static org.sopt.makers.internal.report.domain.EventData.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.sopt.makers.internal.community.repository.CommunityPostLikeRepository;
import org.sopt.makers.internal.community.repository.CommunityPostRepository;
import org.sopt.makers.internal.domain.Word;
import org.sopt.makers.internal.external.MakersCrewClient;
import org.sopt.makers.internal.report.domain.PlaygroundType;
import org.sopt.makers.internal.report.domain.SoptReportCategory;
import org.sopt.makers.internal.report.domain.SoptReportStats;
import org.sopt.makers.internal.report.dto.CrewFastestJoinedGroupResponse;
import org.sopt.makers.internal.report.dto.PlayGroundTypeStatsDto;
import org.sopt.makers.internal.report.dto.response.MySoptReportStatsResponse;
import org.sopt.makers.internal.report.repository.AmplitudeEventRawDataRepository;
import org.sopt.makers.internal.report.repository.SoptReportStatsRepository;
import org.sopt.makers.internal.repository.WordChainGameQueryRepository;
import org.sopt.makers.internal.repository.WordRepository;
import org.sopt.makers.internal.repository.community.CommunityCommentRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import feign.FeignException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SoptReportStatsService {
	private final AmplitudeEventRawDataRepository amplitudeEventRawDataRepository;

	private final SoptReportStatsRepository soptReportStatsRepository;
	private final CommunityPostLikeRepository communityPostLikeRepository;
	private final WordRepository wordRepository;
	private final WordChainGameQueryRepository wordChainGameWinnerRepository;

	private final CommunityPostRepository communityPostRepository;
	private final CommunityCommentRepository communityCommentRepository;

	private final MakersCrewClient makersCrewClient;

	private final Integer CREW_TOP_FASTEST_JOINED_GROUP_LIMIT = 3;

	@Cacheable(cacheNames = TYPE_COMMON_SOPT_REPORT_STATS, key = "#category")
	@Transactional(readOnly = true)
	public Map<String, Object> getSoptReportStats(SoptReportCategory category) {
		return soptReportStatsRepository.findByCategory(category.name()).stream().collect(
			Collectors.toMap(
				SoptReportStats::getTemplateKey,
				stats -> Objects.requireNonNull(serialize(stats.getData()))
			)
		);
	}

	@Cacheable(cacheNames = TYPE_MY_SOPT_REPORT_STATS, key = "#memberId")
	@Transactional(readOnly = true)
	public MySoptReportStatsResponse getMySoptReportStats(Long memberId) {
		long totalVisitCount = amplitudeEventRawDataRepository.countAllByUserIdAndEventTypeAndEventTimeContains(memberId.toString(), TOTAL_VISIT_COUNT.getProperty(), REPORT_FILTER_YEAR.toString());

		// Community
		int likeCount = communityPostLikeRepository.countAllByMemberIdAndCreatedAtBetween(memberId, START_DATE_OF_YEAR, END_DATE_OF_YEAR);

		// WordChainGame
		List<Word> memberWords = wordRepository.findAllByMemberIdAndCreatedAtBetween(memberId, START_DATE_OF_YEAR, END_DATE_OF_YEAR);
		List<String> wordList = getShuffledWordList(memberWords);
		int playCount = memberWords.size();
		int winCount = (int) wordChainGameWinnerRepository.countByUserIdAndCreatedAtBetween(memberId, START_DATE_OF_YEAR, END_DATE_OF_YEAR);

		// Profile
		long viewCount = amplitudeEventRawDataRepository.countAllByUserIdAndEventTypeAndEventTimeContains(memberId.toString(), MEMBER_PROFILE_CARD_VIEW_COUNT.getProperty(), REPORT_FILTER_YEAR.toString());

		// Crew
		List<String> topFastestJoinedGroupList;
		try {
			topFastestJoinedGroupList = makersCrewClient.getFastestAppliedGroups(
				memberId,
				CREW_TOP_FASTEST_JOINED_GROUP_LIMIT,
				REPORT_FILTER_YEAR
			).topFastestAppliedMeetings().stream().map(
				CrewFastestJoinedGroupResponse.CrewGroupDto::title).toList();
		} catch (FeignException ex) {
			topFastestJoinedGroupList = Collections.emptyList();
		}

		return new MySoptReportStatsResponse(
			calculatePlaygroundType(memberId, totalVisitCount, likeCount, playCount).getTitle(),
			totalVisitCount,
			new MySoptReportStatsResponse.CommunityStatsDto(
				likeCount
			),
			new MySoptReportStatsResponse.ProfileStatsDto(
				viewCount
			),
			new MySoptReportStatsResponse.CrewStatsDto(
				topFastestJoinedGroupList
			),
			new MySoptReportStatsResponse.WordChainGameStatsDto(
				playCount, winCount, wordList
			)
		);
	}

	private List<String> getShuffledWordList(List<Word> memberWords) {
		List<String> wordList = memberWords.stream().map(Word::getWord).collect(Collectors.toList());
		Collections.shuffle(wordList);
		return wordList.size() > 6 ? wordList.subList(0, 6) : wordList;
	}

	private PlaygroundType calculatePlaygroundType(Long memberId, long totalVisitCount, int likeCount, int wordChainGamePlayCount) {
		if (totalVisitCount == 0) {
			return PlaygroundType.DEFAULT;
		}

		int postCount = communityPostRepository.countAllByMemberIdAndCreatedAtBetween(memberId, START_DATE_OF_YEAR, END_DATE_OF_YEAR);
		int commentCount = communityCommentRepository.countAllByWriterIdAndCreatedAtBetween(memberId, START_DATE_OF_YEAR, END_DATE_OF_YEAR);

		long memberVisitCount = amplitudeEventRawDataRepository.countByUserIdAndEventTypeAndEventPropertiesPagePathAndEventTime(memberId.toString(), MEMBER_TAB_VISIT_COUNT.getProperty(), MEMBER_TAB_VISIT_COUNT.getPagePath(), REPORT_FILTER_YEAR.toString());
		long projectVisitCount = amplitudeEventRawDataRepository.countByUserIdAndEventTypeAndEventPropertiesPagePathAndEventTime(memberId.toString(), PROJECT_TAB_VISIT_COUNT.getProperty(), PROJECT_TAB_VISIT_COUNT.getPagePath(), REPORT_FILTER_YEAR.toString());
		long coffeeChatVisitCount = amplitudeEventRawDataRepository.countByUserIdAndEventTypeAndEventPropertiesPagePathAndEventTime(memberId.toString(), COFFEE_CHAT_TAB_VISIT_COUNT.getProperty(), COFFEE_CHAT_TAB_VISIT_COUNT.getPagePath(), REPORT_FILTER_YEAR.toString());
//		long crewVisitCount = amplitudeEventRawDataRepository.countAllByUserIdAndEventTypeAndEventPropertiesPagePathAndEventTimeContains(memberId.toString(), CREW_TAB_VISIT_COUNT.getProperty(), CREW_TAB_VISIT_COUNT.getPagePath(), REPORT_FILTER_YEAR.toString());

		return new PlayGroundTypeStatsDto(
			((postCount + commentCount + likeCount) / totalVisitCount) *100,
			(memberVisitCount / totalVisitCount) * 100,
			(projectVisitCount / totalVisitCount) * 100,
			(wordChainGamePlayCount / totalVisitCount) * 100,
			(coffeeChatVisitCount / totalVisitCount) * 100,
			(1 / totalVisitCount) * 100  // TODO Crew Data 수정 필요
		).getTopStats();
	}
}
