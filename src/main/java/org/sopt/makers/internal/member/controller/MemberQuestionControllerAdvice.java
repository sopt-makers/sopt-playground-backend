package org.sopt.makers.internal.member.controller;

import org.sopt.makers.internal.member.converter.StringToQuestionTabConverter;
import org.sopt.makers.internal.member.domain.QuestionTab;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

@ControllerAdvice(assignableTypes = MemberQuestionController.class)
public class MemberQuestionControllerAdvice {

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(QuestionTab.class, new java.beans.PropertyEditorSupport() {
			@Override
			public void setAsText(String text) {
				setValue(new StringToQuestionTabConverter().convert(text));
			}
		});
	}
}
