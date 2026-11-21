package org.sopt.makers.internal.member.service;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.domain.MemberQuestion;
import org.sopt.makers.internal.member.domain.QuestionReaction;
import org.sopt.makers.internal.member.repository.QuestionReactionRepository;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

	public Map<Long, Long> countByQuestionIds(List<Long> questionIds) {
		if (questionIds.isEmpty()) {
			return Map.of();
		}
		return questionReactionRepository.countByQuestionIds(questionIds).stream()
			.collect(Collectors.toMap(
				QuestionReactionRepository.QuestionReactionCountProjection::getQuestionId,
				QuestionReactionRepository.QuestionReactionCountProjection::getReactionCount
			));
	}

	public Set<Long> findReactedQuestionIds(Long memberId, List<Long> questionIds) {
		if (questionIds.isEmpty()) {
			return Set.of();
		}
		return new HashSet<>(questionReactionRepository.findReactedQuestionIds(memberId, questionIds));
	}
}
