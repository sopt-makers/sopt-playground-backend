package org.sopt.makers.internal.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.makers.internal.common.event.PushNotificationEvent;
import org.sopt.makers.internal.exception.BadRequestException;
import org.sopt.makers.internal.exception.ForbiddenException;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.external.platform.PlatformService;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.domain.MemberAnswer;
import org.sopt.makers.internal.member.domain.MemberQuestion;
import org.sopt.makers.internal.member.domain.QuestionTab;
import org.sopt.makers.internal.member.dto.request.*;
import org.sopt.makers.internal.member.dto.response.*;
import org.sopt.makers.internal.community.dto.AnonymousProfileVo;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousNickname;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousProfileImage;
import org.sopt.makers.internal.community.service.anonymous.AnonymousNicknameRetriever;
import org.sopt.makers.internal.community.service.anonymous.AnonymousProfileImageRetriever;
import org.sopt.makers.internal.external.pushNotification.message.member.AnswerNotificationMessage;
import org.sopt.makers.internal.external.sms.SmsNotificationService;
import org.sopt.makers.internal.external.sms.message.member.QuestionNotificationSmsMessage;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberQuestionService {

	private final MemberRetriever memberRetriever;
	private final MemberQuestionRetriever memberQuestionRetriever;
	private final MemberQuestionModifier memberQuestionModifier;
	private final MemberAnswerRetriever memberAnswerRetriever;
	private final MemberAnswerModifier memberAnswerModifier;
	private final QuestionReactionRetriever questionReactionRetriever;
	private final QuestionReactionModifier questionReactionModifier;
	private final AnswerReactionRetriever answerReactionRetriever;
	private final AnswerReactionModifier answerReactionModifier;
	private final QuestionReportRetriever questionReportRetriever;
	private final QuestionReportModifier questionReportModifier;
	private final PlatformService platformService;
	private final SmsNotificationService smsNotificationService;
	private final ApplicationEventPublisher eventPublisher;
	private final AnonymousNicknameRetriever anonymousNicknameRetriever;
	private final AnonymousProfileImageRetriever anonymousProfileImageRetriever;

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
	private static final int NEW_QUESTION_DAYS = 7;

	@Transactional
	public Long createQuestion(Long askerId, Long receiverId, QuestionSaveRequest request) {
		Member asker = memberRetriever.findMemberById(askerId);
		Member receiver = memberRetriever.findMemberById(receiverId);

		if (askerId.equals(receiverId)) {
			throw new BadRequestException("자기 자신에게 질문할 수 없습니다.");
		}

		AnonymousNickname anonymousNickname = null;
		AnonymousProfileImage anonymousProfileImage = null;

		if (request.isAnonymous()) {
			List<MemberQuestion> receiverQuestions = memberQuestionRetriever.findByReceiverId(receiverId);
			List<AnonymousNickname> excludeNicknames = receiverQuestions.stream()
				.map(MemberQuestion::getAnonymousNickname)
				.filter(Objects::nonNull)
				.distinct()
				.toList();

			anonymousNickname = anonymousNicknameRetriever.findRandomAnonymousNickname(excludeNicknames);
			anonymousProfileImage = anonymousProfileImageRetriever.getAnonymousProfileImage();
		}

		MemberQuestion question = memberQuestionModifier.createQuestion(
			receiver,
			asker,
			request.content(),
			request.isAnonymous(),
			anonymousNickname,
			anonymousProfileImage
		);

		sendQuestionNotification(question, receiverId);

		return question.getId();
	}

	@Transactional
	public void updateQuestion(Long userId, Long questionId, QuestionUpdateRequest request) {
		MemberQuestion question = memberQuestionRetriever.findById(questionId);
		memberRetriever.checkExistsMemberById(userId);

		validateQuestionOwner(question, userId);

		if (question.hasAnswer()) {
			throw new BadRequestException("답변이 달린 질문은 수정할 수 없습니다.");
		}

		AnonymousNickname anonymousNickname = null;
		AnonymousProfileImage anonymousProfileImage = null;

		if (request.isAnonymous()) {
			if (question.getIsAnonymous()) {
				anonymousNickname = question.getAnonymousNickname();
				anonymousProfileImage = question.getAnonymousProfileImage();
			} else {
				Long receiverId = question.getReceiver().getId();
				List<MemberQuestion> receiverQuestions = memberQuestionRetriever.findByReceiverId(receiverId);
				List<AnonymousNickname> excludeNicknames = receiverQuestions.stream()
					.map(MemberQuestion::getAnonymousNickname)
					.filter(Objects::nonNull)
					.distinct()
					.toList();

				anonymousNickname = anonymousNicknameRetriever.findRandomAnonymousNickname(excludeNicknames);
				anonymousProfileImage = anonymousProfileImageRetriever.getAnonymousProfileImage();
			}
		}

		memberQuestionModifier.updateQuestion(
			question,
			request.content(),
			request.isAnonymous(),
			anonymousNickname,
			anonymousProfileImage
		);
	}

	@Transactional
	public void deleteQuestion(Long userId, Long questionId) {
		MemberQuestion question = memberQuestionRetriever.findById(questionId);
		memberRetriever.checkExistsMemberById(userId);

		boolean isAsker = question.getAsker().getId().equals(userId);
		boolean isReceiver = question.getReceiver().getId().equals(userId);

		if (!isAsker && !isReceiver) {
			throw new ForbiddenException("질문을 삭제할 권한이 없습니다.");
		}

		if (isAsker && question.hasAnswer()) {
			throw new BadRequestException("답변이 달린 질문은 작성자가 삭제할 수 없습니다.");
		}

		memberQuestionModifier.deleteQuestion(question);
	}

	@Transactional
	public Long createAnswer(Long userId, Long questionId, AnswerSaveRequest request) {
		MemberQuestion question = memberQuestionRetriever.findById(questionId);
		memberRetriever.checkExistsMemberById(userId);

		if (!question.getReceiver().getId().equals(userId)) {
			throw new ForbiddenException("질문을 받은 사람만 답변할 수 있습니다.");
		}

		if (memberAnswerRetriever.existsByQuestion(question)) {
			throw new BadRequestException("이미 답변이 작성된 질문입니다.");
		}

		MemberAnswer answer = memberAnswerModifier.createAnswer(question, request.content());

		sendAnswerNotification(question, answer, userId);

		return answer.getId();
	}

	@Transactional
	public void updateAnswer(Long userId, Long answerId, AnswerUpdateRequest request) {
		MemberAnswer answer = memberAnswerRetriever.findById(answerId);
		memberRetriever.checkExistsMemberById(userId);

		if (!answer.getQuestion().getReceiver().getId().equals(userId)) {
			throw new ForbiddenException("답변 작성자만 수정할 수 있습니다.");
		}

		memberAnswerModifier.updateAnswer(answer, request.content());
	}

	@Transactional
	public void deleteAnswer(Long userId, Long answerId) {
		MemberAnswer answer = memberAnswerRetriever.findById(answerId);
		memberRetriever.checkExistsMemberById(userId);

		if (!answer.getQuestion().getReceiver().getId().equals(userId)) {
			throw new ForbiddenException("답변 작성자만 삭제할 수 있습니다.");
		}

		memberAnswerModifier.deleteAnswer(answer);
	}

	@Transactional
	public void toggleQuestionReaction(Long userId, Long questionId) {
		MemberQuestion question = memberQuestionRetriever.findById(questionId);
		Member user = memberRetriever.findMemberById(userId);

		if (questionReactionRetriever.existsByQuestionAndMember(question, user)) {
			questionReactionModifier.deleteReaction(question, user);
		} else {
			questionReactionModifier.createReaction(question, user);
		}
	}

	@Transactional
	public void toggleAnswerReaction(Long userId, Long answerId) {
		MemberAnswer answer = memberAnswerRetriever.findById(answerId);
		Member user = memberRetriever.findMemberById(userId);

		if (answerReactionRetriever.existsByAnswerAndMember(answer, user)) {
			answerReactionModifier.deleteReaction(answer, user);
		} else {
			answerReactionModifier.createReaction(answer, user);
		}
	}

	@Transactional
	public void reportQuestion(Long userId, Long questionId, QuestionReportRequest request) {
		MemberQuestion question = memberQuestionRetriever.findById(questionId);

		if (questionReportRetriever.existsByQuestionIdAndReporterId(questionId, userId)) {
			throw new BadRequestException("이미 신고한 질문입니다.");
		}

		questionReportModifier.createReport(questionId, userId, request.reason());
		memberQuestionModifier.markAsReported(question);
	}

	@Transactional(readOnly = true)
	public QuestionsResponse getQuestions(Long userId, Long receiverId, QuestionTab tab, Integer page, Integer size) {
		int pageNumber = page != null ? page : 0;
		int pageSize = (size != null && size > 0 && size <= 100) ? size : 10;

		List<MemberQuestion> questions;
		long totalElements;

		if (tab == null) {
			questions = memberQuestionRetriever.findAllQuestions(receiverId, pageNumber, pageSize);
			totalElements = memberQuestionRetriever.countAllQuestions(receiverId);
		} else if (QuestionTab.ANSWERED == tab) {
			questions = memberQuestionRetriever.findAnsweredQuestions(receiverId, pageNumber, pageSize);
			totalElements = memberQuestionRetriever.countAnsweredQuestions(receiverId);
		} else {
			questions = memberQuestionRetriever.findUnansweredQuestions(receiverId, pageNumber, pageSize);
			totalElements = memberQuestionRetriever.countUnansweredQuestions(receiverId);
		}

		List<QuestionResponse> questionResponses = questions.stream()
			.map(q -> toQuestionResponse(q, userId))
			.collect(Collectors.toList());

		int totalPages = (int) Math.ceil((double) totalElements / pageSize);
		boolean hasNext = pageNumber < totalPages - 1;
		boolean hasPrevious = pageNumber > 0;

		return new QuestionsResponse(
			questionResponses,
			pageNumber,
			pageSize,
			totalElements,
			totalPages,
			hasNext,
			hasPrevious
		);
	}

	@Transactional(readOnly = true)
	public UnansweredCountResponse getUnansweredCount(Long userId) {
		long count = memberQuestionRetriever.countUnansweredQuestions(userId);
		return new UnansweredCountResponse(count);
	}

	@Transactional(readOnly = true)
	public MyLatestAnsweredQuestionLocationResponse getMyLatestAnsweredQuestionLocation(Long userId, Long receiverId) {
		if (Objects.equals(userId, receiverId)) {
			throw new BadRequestException("자신에게 질문할 수 없습니다.");
		}

		memberRetriever.checkExistsMemberById(userId);
		memberRetriever.checkExistsMemberById(receiverId);

		List<MemberQuestion> myAnsweredQuestions = memberQuestionRetriever.findAllAnsweredByAskerAndReceiverOrderByLatest(userId, receiverId);

		if (myAnsweredQuestions.isEmpty()) {
			return new MyLatestAnsweredQuestionLocationResponse(null, null, null);
		}

		MemberQuestion latestQuestion = myAnsweredQuestions.get(0);
		List<Long> allAnsweredIds = memberQuestionRetriever.findAllAnsweredQuestionIdsByReceiver(receiverId);

		int targetIndexInTotal = allAnsweredIds.indexOf(latestQuestion.getId());
		if (targetIndexInTotal == -1) {
			return new MyLatestAnsweredQuestionLocationResponse(null, null, null);
		}

		int pageSize = 10;
		int page = targetIndexInTotal / pageSize;
		int index = targetIndexInTotal % pageSize;

		return new MyLatestAnsweredQuestionLocationResponse(latestQuestion.getId(), page, index);
	}

	private void validateQuestionOwner(MemberQuestion question, Long userId) {
		if (question.getAsker() == null || !question.getAsker().getId().equals(userId)) {
			throw new ForbiddenException("질문 작성자만 수정할 수 있습니다.");
		}
	}

	private void sendQuestionNotification(MemberQuestion question, Long receiverId) {
		try {
			InternalUserDetails receiver = platformService.getInternalUser(receiverId);
			String askProfileLink = String.format("https://playground.sopt.org/members/%d/?tab=ask", receiverId);

			QuestionNotificationSmsMessage message = QuestionNotificationSmsMessage.of(
				question.getContent(),
				askProfileLink,
				receiver.phone()
			);

			smsNotificationService.sendSms(message);
		} catch (Exception e) {
			log.error("질문 SMS 알림 발송 실패: questionId={}, receiverId={}, error={}", question.getId(), receiverId, e.getMessage(), e);
		}
	}

	private void sendAnswerNotification(MemberQuestion question, MemberAnswer answer, Long answerWriterId) {
		try {
			InternalUserDetails answerWriter = platformService.getInternalUser(answerWriterId);

			AnswerNotificationMessage message = AnswerNotificationMessage.of(
				question.getAsker().getId(),
				answerWriter.name(),
				answer.getContent(),
				null
			);

			eventPublisher.publishEvent(PushNotificationEvent.of(message));
		} catch (Exception e) {
			log.error("답변 푸시 알림 이벤트 발행 실패: questionId={}, error={}", question.getId(), e.getMessage(), e);
		}
	}

	private QuestionResponse toQuestionResponse(MemberQuestion question, Long currentUserId) {
		Long reactionCount = questionReactionRetriever.countByQuestionId(question.getId());
		Boolean isReacted = questionReactionRetriever.existsByQuestionAndMember(
			question,
			memberRetriever.findMemberById(currentUserId)
		);

		AnswerResponse answerResponse = null;
		if (question.getAnswer() != null) {
			MemberAnswer answer = question.getAnswer();
			InternalUserDetails receiverInfo = platformService.getInternalUser(question.getReceiver().getId());
			Long answerReactionCount = answerReactionRetriever.countByAnswerId(answer.getId());
			Boolean isAnswerReacted = answerReactionRetriever.existsByAnswerAndMember(
				answer,
				memberRetriever.findMemberById(currentUserId)
			);

			answerResponse = new AnswerResponse(
				answer.getId(),
				answer.getContent(),
				answerReactionCount,
				isAnswerReacted,
				receiverInfo.userId(),
				receiverInfo.name(),
				receiverInfo.profileImage(),
				answer.getCreatedAt().format(DATE_TIME_FORMATTER)
			);
		}

		boolean isNew = question.getCreatedAt().isAfter(LocalDateTime.now().minusDays(NEW_QUESTION_DAYS));

		InternalUserDetails askerInfo = platformService.getInternalUser(question.getAsker().getId());

		Long askerId = question.getIsAnonymous() ? null : question.getAsker().getId();
		String askerName = question.getIsAnonymous() ? null : askerInfo.name();
		String askerProfileImage = question.getIsAnonymous() ? null : askerInfo.profileImage();

		String askerLatestGeneration = askerInfo.soptActivities().stream()
			.max((a1, a2) -> Integer.compare(a1.generation(), a2.generation()))
			.map(activity -> activity.generation() + "기 " + activity.part())
			.orElse(null);

		AnonymousProfileVo anonymousProfile = null;
		if (question.getIsAnonymous() && question.getAnonymousNickname() != null) {
			String nickname = question.getAnonymousNickname().getNickname();
			String profileImgUrl = question.getAnonymousProfileImage() != null
				? question.getAnonymousProfileImage().getImageUrl()
				: null;
			anonymousProfile = new AnonymousProfileVo(nickname, profileImgUrl);
		}

		boolean isMine = question.getAsker().getId().equals(currentUserId);
		boolean isReceived = question.getReceiver().getId().equals(currentUserId);

		return new QuestionResponse(
			question.getId(),
			question.getContent(),
			askerId,
			askerName,
			askerProfileImage,
			askerLatestGeneration,
			anonymousProfile,
			question.getIsAnonymous(),
			reactionCount,
			isReacted,
			question.hasAnswer(),
			answerResponse,
			question.getCreatedAt().format(DATE_TIME_FORMATTER),
			isNew,
			isMine,
			isReceived
		);
	}
}
