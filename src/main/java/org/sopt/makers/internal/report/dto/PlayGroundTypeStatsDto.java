package org.sopt.makers.internal.report.dto;

import java.util.Arrays;
import java.util.List;

import org.sopt.makers.internal.report.domain.PlaygroundType;

public record PlayGroundTypeStatsDto(
	Integer community,
	Integer member,
	Integer project,
	Integer wordChainGame,
	Integer coffeeChat,
	Integer crew
) {
	public PlaygroundType getTopStats() {
		List<Integer> values = Arrays.asList(community, member, project, wordChainGame, coffeeChat, crew);
		int max = values.stream().max(Integer::compareTo).orElse(0);

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
