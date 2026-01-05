package org.sopt.makers.internal.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.sopt.makers.internal.common.util.PaginationUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
	private static final int NEW_QUESTION_DAYS = 7;

	@Transactional
	public Long createQuestion(Long askerId, QuestionSaveRequest request) {
		Member asker = memberRetriever.findMemberById(askerId);
		Member receiver = memberRetriever.findMemberById(request.receiverId());

		if (askerId.equals(request.receiverId())) {
			throw new BadRequestException("자기 자신에게 질문할 수 없습니다.");
		}

		MemberQuestion question = memberQuestionModifier.createQuestion(
			receiver,
			asker,
			request.content(),
			request.isAnonymous()
		);

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

		memberQuestionModifier.updateQuestion(question, request.content());
	}

	@Transactional
	public void deleteQuestion(Long userId, Long questionId) {
		MemberQuestion question = memberQuestionRetriever.findById(questionId);
		memberRetriever.checkExistsMemberById(userId);

		boolean isAsker = question.getAsker() != null && question.getAsker().getId().equals(userId);
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

		// TODO: 푸시 알림 발송 (문구 확정 후 구현)
		// if (question.getAsker() != null) {
		//     pushNotificationService.sendAnswerNotification(question.getAsker().getId());
		// }

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

		if (question.hasAnswer()) {
			throw new BadRequestException("답변이 달린 질문에는 '나도 궁금해요'를 누를 수 없습니다.");
		}

		if (question.getReceiver().getId().equals(userId)) {
			throw new BadRequestException("본인에게 달린 질문에는 '나도 궁금해요'를 누를 수 없습니다.");
		}

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

		if (answer.getQuestion().getReceiver().getId().equals(userId)) {
			throw new BadRequestException("본인의 답변에는 '도움돼요'를 누를 수 없습니다.");
		}

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
	public QuestionsResponse getQuestions(Long userId, Long receiverId, QuestionTab tab, Long cursor, Integer limit) {
		int queryLimit = PaginationUtil.validateAndGetLimit(limit);

		List<MemberQuestion> questions;
		if (QuestionTab.ANSWERED == tab) {
			questions = memberQuestionRetriever.findAnsweredQuestions(receiverId, cursor, queryLimit + 1);
		} else {
			questions = memberQuestionRetriever.findUnansweredQuestions(receiverId, cursor, queryLimit + 1);
		}

		boolean hasNext = questions.size() > queryLimit;
		List<MemberQuestion> limitedQuestions = hasNext ?
			questions.subList(0, queryLimit) : questions;

		Long nextCursor = hasNext && !limitedQuestions.isEmpty() ?
			limitedQuestions.get(limitedQuestions.size() - 1).getId() : null;

		List<QuestionResponse> questionResponses = limitedQuestions.stream()
			.map(q -> toQuestionResponse(q, userId))
			.collect(Collectors.toList());

		return new QuestionsResponse(questionResponses, hasNext, nextCursor);
	}

	@Transactional(readOnly = true)
	public UnansweredCountResponse getUnansweredCount(Long userId) {
		long count = memberQuestionRetriever.countUnansweredQuestions(userId);
		return new UnansweredCountResponse(count);
	}

	private void validateQuestionOwner(MemberQuestion question, Long userId) {
		if (question.getIsAnonymous()) {
			throw new ForbiddenException("익명 질문은 수정할 수 없습니다.");
		}

		if (question.getAsker() == null || !question.getAsker().getId().equals(userId)) {
			throw new ForbiddenException("질문 작성자만 수정할 수 있습니다.");
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
				answer.getCreatedAt().format(DATE_TIME_FORMATTER)
			);
		}

		boolean isNew = question.getCreatedAt().isAfter(LocalDateTime.now().minusDays(NEW_QUESTION_DAYS));

		Long askerId = null;
		String askerName = null;
		String askerProfileImage = null;

		if (!question.getIsAnonymous() && question.getAsker() != null) {
			InternalUserDetails askerInfo = platformService.getInternalUser(question.getAsker().getId());
			askerId = question.getAsker().getId();
			askerName = askerInfo.name();
			askerProfileImage = askerInfo.profileImage();
		}

		boolean isMine = question.getAsker() != null && question.getAsker().getId().equals(currentUserId);
		boolean isReceived = question.getReceiver().getId().equals(currentUserId);

		return new QuestionResponse(
			question.getId(),
			question.getContent(),
			askerId,
			askerName,
			askerProfileImage,
			null,
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
