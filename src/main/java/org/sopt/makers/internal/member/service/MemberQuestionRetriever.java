package org.sopt.makers.internal.member.service;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.exception.NotFoundException;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.domain.MemberQuestion;
import org.sopt.makers.internal.member.repository.MemberQuestionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MemberQuestionRetriever {

	private final MemberQuestionRepository memberQuestionRepository;

	public MemberQuestion findById(Long questionId) {
		return memberQuestionRepository.findById(questionId)
			.orElseThrow(() -> new NotFoundException("존재하지 않는 질문입니다. id: [" + questionId + "]"));
	}

	public List<MemberQuestion> findAnsweredQuestions(Long receiverId, Long cursor, int limit) {
		return memberQuestionRepository.findAnsweredQuestions(receiverId, cursor, PageRequest.of(0, limit));
	}

	public List<MemberQuestion> findUnansweredQuestions(Long receiverId, Long cursor, int limit) {
		return memberQuestionRepository.findUnansweredQuestions(receiverId, cursor, PageRequest.of(0, limit));
	}

	public long countUnansweredQuestions(Long receiverId) {
		return memberQuestionRepository.countUnansweredQuestions(receiverId);
	}

	public boolean isAsker(Long questionId, Member asker) {
		return memberQuestionRepository.existsByIdAndAsker(questionId, asker);
	}

	public boolean isReceiver(Long questionId, Member receiver) {
		return memberQuestionRepository.existsByIdAndReceiver(questionId, receiver);
	}
}
