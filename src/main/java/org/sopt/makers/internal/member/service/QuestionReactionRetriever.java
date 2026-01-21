package org.sopt.makers.internal.member.service;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.domain.MemberQuestion;
import org.sopt.makers.internal.member.domain.QuestionReaction;
import org.sopt.makers.internal.member.repository.QuestionReactionRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class QuestionReactionRetriever {

	private final QuestionReactionRepository questionReactionRepository;

	public Optional<QuestionReaction> findByQuestionAndMember(MemberQuestion question, Member member) {
		return questionReactionRepository.findByQuestionAndMember(question, member);
	}

	public boolean existsByQuestionAndMember(MemberQuestion question, Member member) {
		return questionReactionRepository.existsByQuestionAndMember(question, member);
	}

	public long countByQuestionId(Long questionId) {
		return questionReactionRepository.countByQuestionId(questionId);
	}
}
