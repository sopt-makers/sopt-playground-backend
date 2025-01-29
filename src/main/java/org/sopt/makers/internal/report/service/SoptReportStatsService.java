package org.sopt.makers.internal.report.service;

import static org.sopt.makers.internal.common.Constant.*;
import static org.sopt.makers.internal.common.JsonDataSerializer.*;
import static org.sopt.makers.internal.config.cache.CacheConstant.*;
import static org.sopt.makers.internal.external.amplitude.EventData.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.sopt.makers.internal.community.repository.CommunityPostLikeRepository;
import org.sopt.makers.internal.community.repository.CommunityPostRepository;
import org.sopt.makers.internal.domain.Word;
import org.sopt.makers.internal.external.amplitude.AmplitudeService;
import org.sopt.makers.internal.report.domain.PlaygroundType;
import org.sopt.makers.internal.report.domain.SoptReportCategory;
import org.sopt.makers.internal.report.domain.SoptReportStats;
import org.sopt.makers.internal.report.dto.PlayGroundTypeStatsDto;
import org.sopt.makers.internal.report.dto.response.MySoptReportStatsResponse;
import org.sopt.makers.internal.report.repository.SoptReportStatsRepository;
import org.sopt.makers.internal.repository.WordChainGameQueryRepository;
import org.sopt.makers.internal.repository.WordRepository;
import org.sopt.makers.internal.repository.community.CommunityCommentRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SoptReportStatsService {
	private final AmplitudeService amplitudeService;

	private final SoptReportStatsRepository soptReportStatsRepository;
	private final CommunityPostLikeRepository communityPostLikeRepository;
	private final WordRepository wordRepository;
	private final WordChainGameQueryRepository wordChainGameWinnerRepository;

	private final CommunityPostRepository communityPostRepository;
	private final CommunityCommentRepository communityCommentRepository;

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
		Map<String, Long> events = amplitudeService.getUserEventData(memberId);

		long totalVisitCount = events.get(generateEventKey(TOTAL_VISIT_COUNT));

		// Community
		int likeCount = communityPostLikeRepository.countAllByMemberIdAndCreatedAtBetween(memberId, START_DATE_OF_YEAR, END_DATE_OF_YEAR);

		// WordChainGame
		List<Word> memberWords = wordRepository.findAllByMemberIdAndCreatedAtBetween(memberId, START_DATE_OF_YEAR, END_DATE_OF_YEAR);
		List<String> wordList = getShuffledWordList(memberWords);
		int playCount = memberWords.size();
		int winCount = (int) wordChainGameWinnerRepository.countByUserIdAndCreatedAtBetween(memberId, START_DATE_OF_YEAR, END_DATE_OF_YEAR);

		// Profile
		long viewCount = events.get(generateEventKey(MEMBER_PROFILE_CARD_VIEW_COUNT));
		List<String> topFastestJoinedGroupList = Arrays.asList("모임1","모임2");

		return new MySoptReportStatsResponse(
			calculatePlaygroundType(memberId, events, likeCount, playCount).getTitle(),
			totalVisitCount,
			new MySoptReportStatsResponse.CommunityStatsDto(
				likeCount
			),
			new MySoptReportStatsResponse.ProfileStatsDto(
				viewCount
			),
			new MySoptReportStatsResponse.CrewStatsDto(
				topFastestJoinedGroupList  // TODO Crew API 응답 활용
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

	private PlaygroundType calculatePlaygroundType(Long memberId, Map<String, Long> events, int likeCount, int wordChainGamePlayCount) {
		// Playground Visit Count (Amplitude)
		long totalVisitCount = events.get(generateEventKey(TOTAL_VISIT_COUNT));
		if (totalVisitCount == 0) {
			return PlaygroundType.DEFAULT;
		}

		int postCount = communityPostRepository.countAllByMemberIdAndCreatedAtBetween(memberId, START_DATE_OF_YEAR, END_DATE_OF_YEAR);
		int commentCount = communityCommentRepository.countAllByWriterIdAndCreatedAtBetween(memberId, START_DATE_OF_YEAR, END_DATE_OF_YEAR);

		long memberVisitCount = events.get(generateEventKey(MEMBER_TAB_VISIT_COUNT));
		long projectVisitCount = events.get(generateEventKey(PROJECT_TAB_VISIT_COUNT));
		long coffeeChatVisitCount = events.get(generateEventKey(COFFEE_CHAT_TAB_VISIT_COUNT));
		long crewVisitCount = 1; // TODO AMpl

		return new PlayGroundTypeStatsDto(
			((postCount + commentCount + likeCount) / totalVisitCount) *100,
			(memberVisitCount / totalVisitCount) * 100,
			(projectVisitCount / totalVisitCount) * 100,
			(wordChainGamePlayCount / totalVisitCount) * 100,
			(coffeeChatVisitCount / totalVisitCount) * 100,
			(crewVisitCount / totalVisitCount) * 100
		).getTopStats();
	}
}
