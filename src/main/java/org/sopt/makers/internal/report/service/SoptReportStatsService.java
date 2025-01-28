package org.sopt.makers.internal.report.service;

import static org.sopt.makers.internal.common.JsonDataSerializer.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.sopt.makers.internal.community.repository.CommunityPostLikeRepository;
import org.sopt.makers.internal.community.repository.CommunityPostRepository;
import org.sopt.makers.internal.domain.Word;
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
	private final SoptReportStatsRepository soptReportStatsRepository;
	private final CommunityPostLikeRepository communityPostLikeRepository;
	private final WordRepository wordRepository;
	private final WordChainGameQueryRepository wordChainGameWinnerRepository;

	private final Integer REPORT_FILTER_YEAR = 2024;
	private final LocalDateTime startDate = LocalDateTime.of(REPORT_FILTER_YEAR, 1, 1, 0, 0);
	private final LocalDateTime endDate = LocalDateTime.of(REPORT_FILTER_YEAR, 12, 31, 23, 59);
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
		int likeCount = communityPostLikeRepository.countAllByMemberIdAndCreatedAtBetween(memberId, startDate, endDate);

		// WordChainGame
		List<Word> memberWords = wordRepository.findAllByMemberIdAndCreatedAtBetween(memberId, startDate, endDate);
		List<String> wordList = getShuffledWordList(memberWords);
		int playCount = memberWords.size();
		int winCount = (int) wordChainGameWinnerRepository.countByUserIdAndCreatedAtBetween(memberId, startDate, endDate);

		int viewCount = 100;
		List<String> topFastestJoinedGroupList = Arrays.asList("모임1","모임2");

		return new MySoptReportStatsResponse(
			calculatePlaygroundType(memberId).getTitle(),
			100, // TODO Amplitude
			100, // TODO Amplitude,
			new MySoptReportStatsResponse.CommunityStatsDto(
				likeCount
			),
			new MySoptReportStatsResponse.ProfileStatsDto(
				viewCount // TODO Amplitude
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

	private PlaygroundType calculatePlaygroundType(Long memberId) {
		// Playground Visit Count (Amplitude)
		int totalVisitCount = 100;

		// Community
		int postCount = communityPostRepository.countAllByMemberIdAndCreatedAtBetween(memberId, startDate, endDate);
		int commentCount = communityCommentRepository.countAllByWriterIdAndCreatedAtBetween(memberId, startDate, endDate);
		int likeCount = communityPostLikeRepository.countAllByMemberIdAndCreatedAtBetween(memberId, startDate, endDate);

		// Member
		int memberVisitCount = 100; // TODO Ampl

		// Project
		int projectVisitCount = 100; // TODO AMpl

		// WordChainGame
		int wordChainGameVisitCount = 100; // TODO Ampl

		// CoffeeChat
		int coffeeChatVisitCount = 100; // TODO Ampl

		// Crew
		int crewVisitCount = 100; // TODO AMpl

		return new PlayGroundTypeStatsDto(
			((postCount + commentCount + likeCount) / totalVisitCount) *100,
			(memberVisitCount / totalVisitCount) * 100,
			(projectVisitCount / totalVisitCount) * 100,
			(wordChainGameVisitCount / totalVisitCount) * 100,
			(coffeeChatVisitCount / totalVisitCount) * 100,
			(crewVisitCount / totalVisitCount) * 100
		).getTopStats();
	}

}
