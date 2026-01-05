package org.sopt.makers.internal.member.service;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.exception.NotFoundException;
import org.sopt.makers.internal.member.domain.MemberAnswer;
import org.sopt.makers.internal.member.domain.MemberQuestion;
import org.sopt.makers.internal.member.repository.MemberAnswerRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MemberAnswerRetriever {

	private final MemberAnswerRepository memberAnswerRepository;

	public MemberAnswer findById(Long answerId) {
		return memberAnswerRepository.findById(answerId)
			.orElseThrow(() -> new NotFoundException("존재하지 않는 답변입니다. id: [" + answerId + "]"));
	}

	public Optional<MemberAnswer> findByQuestion(MemberQuestion question) {
		return memberAnswerRepository.findByQuestion(question);
	}

	public boolean existsByQuestion(MemberQuestion question) {
		return memberAnswerRepository.existsByQuestion(question);
	}
}
