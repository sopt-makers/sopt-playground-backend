package org.sopt.makers.internal.member.service;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.member.domain.AnswerReaction;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.domain.MemberAnswer;
import org.sopt.makers.internal.member.repository.AnswerReactionRepository;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

	public Map<Long, Long> countByAnswerIds(List<Long> answerIds) {
		if (answerIds.isEmpty()) {
			return Map.of();
		}
		return answerReactionRepository.countByAnswerIds(answerIds).stream()
			.collect(Collectors.toMap(
				AnswerReactionRepository.AnswerReactionCountProjection::getAnswerId,
				AnswerReactionRepository.AnswerReactionCountProjection::getReactionCount
			));
	}

	public Set<Long> findReactedAnswerIds(Long memberId, List<Long> answerIds) {
		if (answerIds.isEmpty()) {
			return Set.of();
		}
		return new HashSet<>(answerReactionRepository.findReactedAnswerIds(memberId, answerIds));
	}
}
