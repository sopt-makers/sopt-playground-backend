package org.sopt.makers.internal.member.converter;

import org.jetbrains.annotations.NotNull;
import org.sopt.makers.internal.member.domain.QuestionTab;
import org.springframework.core.convert.converter.Converter;

public class StringToQuestionTabConverter implements Converter<String, QuestionTab> {

	@Override
	public QuestionTab convert(@NotNull String source) {
		return QuestionTab.from(source);
	}
}
