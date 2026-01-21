package org.sopt.makers.internal.member.service;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.member.domain.AnswerReaction;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.domain.MemberAnswer;
import org.sopt.makers.internal.member.repository.AnswerReactionRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AnswerReactionModifier {

	private final AnswerReactionRepository answerReactionRepository;

	public AnswerReaction createReaction(MemberAnswer answer, Member member) {
		return answerReactionRepository.save(AnswerReaction.builder()
			.answer(answer)
			.member(member)
			.build());
	}

	@Transactional
	public void deleteReaction(MemberAnswer answer, Member member) {
		answerReactionRepository.deleteByAnswerAndMember(answer, member);
	}
}
