package org.sopt.makers.internal.report.service;

import static org.sopt.makers.internal.common.Constant.*;
import static org.sopt.makers.internal.common.JsonDataSerializer.*;

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
import org.sopt.makers.internal.external.amplitude.EventData;
import org.sopt.makers.internal.report.domain.PlaygroundType;
import org.sopt.makers.internal.report.domain.SoptReportStats;
import org.sopt.makers.internal.report.dto.PlayGroundTypeStatsDto;
import org.sopt.makers.internal.report.dto.response.MySoptReportStatsResponse;
import org.sopt.makers.internal.report.repository.SoptReportStatsRepository;
import org.sopt.makers.internal.repository.WordChainGameQueryRepository;
import org.sopt.makers.internal.repository.WordRepository;
import org.sopt.makers.internal.repository.community.CommunityCommentRepository;
import org.springframework.stereotype.Service;

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

	public Map<String, Object> getSoptReportStats(String category) {
		return soptReportStatsRepository.findByCategory(category).stream().collect(
			Collectors.toMap(
				SoptReportStats::getTemplateKey,
				stats -> Objects.requireNonNull(serialize(stats.getData()))
			)
		);
	}

	public MySoptReportStatsResponse getMySoptReportStats(Long memberId) {
		// Community
		int likeCount = communityPostLikeRepository.countAllByMemberIdAndCreatedAtBetween(memberId, START_DATE_OF_YEAR, END_DATE_OF_YEAR);

		// WordChainGame
		List<Word> memberWords = wordRepository.findAllByMemberIdAndCreatedAtBetween(memberId, START_DATE_OF_YEAR, END_DATE_OF_YEAR);
		List<String> wordList = getShuffledWordList(memberWords);
		int playCount = memberWords.size();
		int winCount = (int) wordChainGameWinnerRepository.countByUserIdAndCreatedAtBetween(memberId, START_DATE_OF_YEAR, END_DATE_OF_YEAR);

		long viewCount = 100; // TODO Amplitude
		List<String> topFastestJoinedGroupList = Arrays.asList("모임1","모임2");

		return new MySoptReportStatsResponse(
			calculatePlaygroundType(memberId, likeCount, playCount).getTitle(),
			100L, // TODO Amplitude
			100L, // TODO Amplitude
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

	private PlaygroundType calculatePlaygroundType(Long memberId, int likeCount, int wordChainGamePlayCount) {
		// Playground Visit Count (Amplitude)
		Map<String, Long> events = amplitudeService.getUserEventData(memberId);
		System.out.println("events: " + events);
		long totalVisitCount = 100;

		int postCount = communityPostRepository.countAllByMemberIdAndCreatedAtBetween(memberId, START_DATE_OF_YEAR, END_DATE_OF_YEAR);
		int commentCount = communityCommentRepository.countAllByWriterIdAndCreatedAtBetween(memberId, START_DATE_OF_YEAR, END_DATE_OF_YEAR);

		long memberVisitCount = events.get(EventData.MEMBER_TAB_VISIT_COUNT.getProperty());
		long projectVisitCount = 100; // TODO AMpl
		long coffeeChatVisitCount = events.get(EventData.COFFEE_CHAT_TAB_VISIT_COUNT.getProperty());
		long crewVisitCount = 100; // TODO AMpl

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
