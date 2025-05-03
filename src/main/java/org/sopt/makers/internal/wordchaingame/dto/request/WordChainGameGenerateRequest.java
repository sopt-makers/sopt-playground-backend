package org.sopt.makers.internal.wordchaingame.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record WordChainGameGenerateRequest(
		@Schema(required = true)
		Long roomId,
		String word
){}
