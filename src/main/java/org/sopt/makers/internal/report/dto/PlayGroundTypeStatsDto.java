package org.sopt.makers.internal.report.dto;

import java.util.Arrays;
import java.util.List;

import org.sopt.makers.internal.report.domain.PlaygroundType;

public record PlayGroundTypeStatsDto(
	Long community,
	Long member,
	Long project,
	Long wordChainGame,
	Long coffeeChat,
	Long crew
) {
	public PlaygroundType getTopStats() {
		List<Long> values = Arrays.asList(community, member, project, wordChainGame, coffeeChat, crew);
		long max = values.stream().max(Long::compareTo).orElse(0L);

		if (max == community) {
			return PlaygroundType.COMMUNITY;
		} else if (max == member) {
			return PlaygroundType.MEMBER;
		} else if (max == project) {
			return PlaygroundType.PROJECT;
		} else if (max == wordChainGame) {
			return PlaygroundType.WORD_CHAIN_GAME;
		} else if (max == coffeeChat) {
			return PlaygroundType.COFFEE_CHAT;
		} else {
			return PlaygroundType.CREW;
		}
	}
}
