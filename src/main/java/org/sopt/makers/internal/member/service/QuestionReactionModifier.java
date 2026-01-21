package org.sopt.makers.internal.member.service;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.domain.MemberQuestion;
import org.sopt.makers.internal.member.domain.QuestionReaction;
import org.sopt.makers.internal.member.repository.QuestionReactionRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class QuestionReactionModifier {

	private final QuestionReactionRepository questionReactionRepository;

	public QuestionReaction createReaction(MemberQuestion question, Member member) {
		return questionReactionRepository.save(QuestionReaction.builder()
			.question(question)
			.member(member)
			.build());
	}

	@Transactional
	public void deleteReaction(MemberQuestion question, Member member) {
		questionReactionRepository.deleteByQuestionAndMember(question, member);
	}
}
