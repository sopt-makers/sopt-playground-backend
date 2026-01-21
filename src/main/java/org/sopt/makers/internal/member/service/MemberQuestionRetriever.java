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

	public List<MemberQuestion> findAnsweredQuestions(Long receiverId, int page, int size) {
		return memberQuestionRepository.findAnsweredQuestions(receiverId, PageRequest.of(page, size));
	}

	public List<MemberQuestion> findUnansweredQuestions(Long receiverId, int page, int size) {
		return memberQuestionRepository.findUnansweredQuestions(receiverId, PageRequest.of(page, size));
	}

	public List<MemberQuestion> findAllQuestions(Long receiverId, int page, int size) {
		return memberQuestionRepository.findAllQuestions(receiverId, PageRequest.of(page, size));
	}

	public long countAnsweredQuestions(Long receiverId) {
		return memberQuestionRepository.countAnsweredQuestions(receiverId);
	}

	public long countUnansweredQuestions(Long receiverId) {
		return memberQuestionRepository.countUnansweredQuestions(receiverId);
	}

	public long countAllQuestions(Long receiverId) {
		return memberQuestionRepository.countAllQuestions(receiverId);
	}

	public List<MemberQuestion> findAllAnsweredByAskerAndReceiverOrderByLatest(Long askerId, Long receiverId) {
		return memberQuestionRepository.findAllAnsweredByAskerAndReceiverOrderByLatest(askerId, receiverId);
	}

	public List<Long> findAllAnsweredQuestionIdsByReceiver(Long receiverId) {
		return memberQuestionRepository.findAllAnsweredQuestionIdsByReceiver(receiverId);
	}

	public List<MemberQuestion> findByReceiverId(Long receiverId) {
		return memberQuestionRepository.findByReceiverId(receiverId);
	}
}
