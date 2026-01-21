package org.sopt.makers.internal.member.service;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousNickname;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousProfileImage;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.domain.MemberQuestion;
import org.sopt.makers.internal.member.repository.MemberQuestionRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberQuestionModifier {

	private final MemberQuestionRepository memberQuestionRepository;

	public MemberQuestion createQuestion(
		Member receiver,
		Member asker,
		String content,
		Boolean isAnonymous,
		AnonymousNickname anonymousNickname,
		AnonymousProfileImage anonymousProfileImage
	) {
		return memberQuestionRepository.save(MemberQuestion.builder()
			.receiver(receiver)
			.asker(asker)
			.content(content)
			.isAnonymous(isAnonymous)
			.anonymousNickname(anonymousNickname)
			.anonymousProfileImage(anonymousProfileImage)
			.build());
	}

	public void updateQuestion(
		MemberQuestion question,
		String content,
		Boolean isAnonymous,
		AnonymousNickname anonymousNickname,
		AnonymousProfileImage anonymousProfileImage
	) {
		question.updateContent(content);
		question.updateAnonymous(isAnonymous, anonymousNickname, anonymousProfileImage);
		memberQuestionRepository.save(question);
	}

	public void deleteQuestion(MemberQuestion question) {
		memberQuestionRepository.delete(question);
	}

	public void markAsReported(MemberQuestion question) {
		question.markAsReported();
		memberQuestionRepository.save(question);
	}
}
