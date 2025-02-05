package org.sopt.makers.internal.report.dto;

import java.util.Arrays;
import java.util.List;

import org.sopt.makers.internal.report.domain.PlaygroundType;

public record PlayGroundTypeStatsDto(
	double community,
	double member,
	double project,
	double wordChainGame,
	double coffeeChat,
	double crew
) {
	public PlaygroundType getTopStats() {
		List<Double> values = Arrays.asList(community, member, project, wordChainGame, coffeeChat, crew);
		double max = values.stream().max(Double::compareTo).orElse(0.0);
		System.out.println(values);

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
