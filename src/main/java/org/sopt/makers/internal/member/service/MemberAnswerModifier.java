package org.sopt.makers.internal.member.service;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.member.domain.MemberAnswer;
import org.sopt.makers.internal.member.domain.MemberQuestion;
import org.sopt.makers.internal.member.repository.MemberAnswerRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberAnswerModifier {

	private final MemberAnswerRepository memberAnswerRepository;

	public MemberAnswer createAnswer(MemberQuestion question, String content) {
		return memberAnswerRepository.save(MemberAnswer.builder()
			.question(question)
			.content(content)
			.build());
	}

	public void updateAnswer(MemberAnswer answer, String content) {
		answer.updateContent(content);
		memberAnswerRepository.save(answer);
	}

	public void deleteAnswer(MemberAnswer answer) {
		memberAnswerRepository.delete(answer);
	}
}
