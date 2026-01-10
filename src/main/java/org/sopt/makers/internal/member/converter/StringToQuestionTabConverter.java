package org.sopt.makers.internal.member.converter;

import org.sopt.makers.internal.member.domain.QuestionTab;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToQuestionTabConverter implements Converter<String, QuestionTab> {

	@Override
	public QuestionTab convert(String source) {
		return QuestionTab.from(source);
	}
}
