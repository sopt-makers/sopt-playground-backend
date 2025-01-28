package org.sopt.makers.internal.report.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PlaygroundType {
	COMMUNITY("솝플루언서"),
	MEMBER("인간 솝크드인"),
	PROJECT("서비스 익솝플로러"),
	WORD_CHAIN_GAME("우리말 솝고수"),
	COFFEE_CHAT("얼죽솝"),
	GROUP("솝만추");

	private final String title;
}
