package org.sopt.makers.internal.member.service;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.member.domain.AnswerReaction;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.domain.MemberAnswer;
import org.sopt.makers.internal.member.repository.AnswerReactionRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AnswerReactionRetriever {

	private final AnswerReactionRepository answerReactionRepository;

	public Optional<AnswerReaction> findByAnswerAndMember(MemberAnswer answer, Member member) {
		return answerReactionRepository.findByAnswerAndMember(answer, member);
	}

	public boolean existsByAnswerAndMember(MemberAnswer answer, Member member) {
		return answerReactionRepository.existsByAnswerAndMember(answer, member);
	}

	public long countByAnswerId(Long answerId) {
		return answerReactionRepository.countByAnswerId(answerId);
	}
}
