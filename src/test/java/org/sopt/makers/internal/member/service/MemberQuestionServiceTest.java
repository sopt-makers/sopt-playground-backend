package org.sopt.makers.internal.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.makers.internal.exception.NotFoundException;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.external.platform.PlatformService;
import org.sopt.makers.internal.external.platform.SoptActivity;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.domain.MemberAnswer;
import org.sopt.makers.internal.member.domain.MemberQuestion;
import org.sopt.makers.internal.member.dto.response.QuestionResponse;
import org.sopt.makers.internal.member.dto.response.QuestionsResponse;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MemberQuestionServiceTest {

	private static final Long USER_ID = 100L;
	private static final Long RECEIVER_ID = 1L;
	private static final Long ASKER_ID = 2L;
	private static final Long ANONYMOUS_ASKER_ID = 3L;

	@Mock
	private MemberRetriever memberRetriever;
	@Mock
	private MemberQuestionRetriever memberQuestionRetriever;
	@Mock
	private QuestionReactionRetriever questionReactionRetriever;
	@Mock
	private AnswerReactionRetriever answerReactionRetriever;
	@Mock
	private PlatformService platformService;

	@InjectMocks
	private MemberQuestionService memberQuestionService;

	private final Member receiver = Member.builder().id(RECEIVER_ID).build();
	private final Member asker = Member.builder().id(ASKER_ID).build();
	private final Member anonymousAsker = Member.builder().id(ANONYMOUS_ASKER_ID).build();

	@Test
	@DisplayName("질문 목록의 작성자·답변자 정보는 중복 제거 후 플랫폼에서 한 번에 조회한다")
	void getQuestions_FetchesUserDetailsInBatch() {
		// Given
		MemberQuestion answered = question(10L, asker, false);
		answer(answered);
		MemberQuestion unanswered = question(11L, asker, false);
		MemberQuestion anonymous = question(12L, anonymousAsker, true);
		givenQuestions(List.of(answered, unanswered, anonymous));

		when(platformService.getInternalUsersOrThrow(List.of(ASKER_ID, ANONYMOUS_ASKER_ID, RECEIVER_ID))).thenReturn(List.of(
			userDetails(RECEIVER_ID, "답변자", 36),
			userDetails(ASKER_ID, "작성자", 37),
			userDetails(ANONYMOUS_ASKER_ID, "익명작성자", 35)
		));

		// When
		QuestionsResponse response = memberQuestionService.getQuestions(USER_ID, RECEIVER_ID, null, 0, 10);

		// Then
		List<QuestionResponse> questions = response.questions();
		assertThat(questions.get(0).askerName()).isEqualTo("작성자");
		assertThat(questions.get(0).askerLatestGeneration()).isEqualTo("37기 서버");
		assertThat(questions.get(0).answer().name()).isEqualTo("답변자");

		// 익명 질문은 이름·이미지를 가리지만 최근 기수 정보는 기존처럼 응답한다.
		assertThat(questions.get(2).askerId()).isNull();
		assertThat(questions.get(2).askerName()).isNull();
		assertThat(questions.get(2).askerLatestGeneration()).isEqualTo("35기 서버");

		verify(platformService, times(1)).getInternalUsersOrThrow(List.of(ASKER_ID, ANONYMOUS_ASKER_ID, RECEIVER_ID));
		verify(platformService, never()).getInternalUser(any());
		verify(platformService, never()).getInternalUsers(any());
	}

	@Test
	@DisplayName("답변이 달린 질문이 없으면 답변자 정보는 조회하지 않는다")
	void getQuestions_ExcludesReceiverWhenNoAnswer() {
		// Given
		givenQuestions(List.of(question(10L, asker, false)));
		when(platformService.getInternalUsersOrThrow(List.of(ASKER_ID)))
			.thenReturn(List.of(userDetails(ASKER_ID, "작성자", 37)));

		// When
		memberQuestionService.getQuestions(USER_ID, RECEIVER_ID, null, 0, 10);

		// Then
		verify(platformService).getInternalUsersOrThrow(List.of(ASKER_ID));
	}

	@Test
	@DisplayName("플랫폼 응답에서 누락된 사용자가 있으면 단건 재조회 없이 NotFoundException이 발생한다")
	void getQuestions_ThrowsNotFoundWhenUserDetailsMissing() {
		// Given
		givenQuestions(List.of(question(10L, asker, false), question(11L, anonymousAsker, true)));
		when(platformService.getInternalUsersOrThrow(List.of(ASKER_ID, ANONYMOUS_ASKER_ID)))
			.thenReturn(List.of(userDetails(ASKER_ID, "작성자", 37)));

		// When & Then
		assertThatThrownBy(() -> memberQuestionService.getQuestions(USER_ID, RECEIVER_ID, null, 0, 10))
			.isInstanceOf(NotFoundException.class);
		verify(platformService, never()).getInternalUser(any());
	}

	@Test
	@DisplayName("플랫폼 통신 장애는 NotFoundException으로 바꾸지 않고 그대로 전파한다")
	void getQuestions_PropagatesPlatformFailure() {
		// Given
		givenQuestions(List.of(question(10L, asker, false)));
		RuntimeException platformFailure = new RuntimeException("platform timeout");
		when(platformService.getInternalUsersOrThrow(List.of(ASKER_ID))).thenThrow(platformFailure);

		// When & Then
		assertThatThrownBy(() -> memberQuestionService.getQuestions(USER_ID, RECEIVER_ID, null, 0, 10))
			.isSameAs(platformFailure);
	}

	@Test
	@DisplayName("질문·답변 반응 정보는 목록 단위로 한 번씩 조회하고, 반응이 없으면 0과 false로 응답한다")
	void getQuestions_FetchesReactionsInBatch() {
		// Given
		MemberQuestion reactedQuestion = question(10L, asker, false);
		answer(reactedQuestion);
		MemberQuestion noReactionQuestion = question(11L, asker, false);
		answer(noReactionQuestion);
		givenQuestions(List.of(reactedQuestion, noReactionQuestion));

		List<Long> questionIds = List.of(10L, 11L);
		List<Long> answerIds = List.of(100L, 110L);
		when(questionReactionRetriever.countByQuestionIds(questionIds)).thenReturn(Map.of(10L, 3L));
		when(questionReactionRetriever.findReactedQuestionIds(USER_ID, questionIds)).thenReturn(Set.of(10L));
		when(answerReactionRetriever.countByAnswerIds(answerIds)).thenReturn(Map.of(100L, 2L));
		when(answerReactionRetriever.findReactedAnswerIds(USER_ID, answerIds)).thenReturn(Set.of());
		when(platformService.getInternalUsersOrThrow(List.of(ASKER_ID, RECEIVER_ID))).thenReturn(List.of(
			userDetails(RECEIVER_ID, "답변자", 36),
			userDetails(ASKER_ID, "작성자", 37)
		));

		// When
		List<QuestionResponse> questions = memberQuestionService.getQuestions(USER_ID, RECEIVER_ID, null, 0, 10)
			.questions();

		// Then
		assertThat(questions.get(0).reactionCount()).isEqualTo(3L);
		assertThat(questions.get(0).isReacted()).isTrue();
		assertThat(questions.get(0).answer().reactionCount()).isEqualTo(2L);
		assertThat(questions.get(0).answer().isReacted()).isFalse();
		assertThat(questions.get(1).reactionCount()).isZero();
		assertThat(questions.get(1).isReacted()).isFalse();
		assertThat(questions.get(1).answer().reactionCount()).isZero();
		assertThat(questions.get(1).answer().isReacted()).isFalse();

		verify(memberRetriever, times(1)).checkExistsMemberById(USER_ID);
		verify(memberRetriever, never()).findMemberById(any());
		verify(questionReactionRetriever, never()).existsByQuestionAndMember(any(), any());
		verify(answerReactionRetriever, never()).existsByAnswerAndMember(any(), any());
	}

	@Test
	@DisplayName("질문이 없으면 플랫폼을 호출하지 않는다")
	void getQuestions_SkipsPlatformCallWhenNoQuestion() {
		// Given
		givenQuestions(List.of());

		// When
		QuestionsResponse response = memberQuestionService.getQuestions(USER_ID, RECEIVER_ID, null, 0, 10);

		// Then
		assertThat(response.questions()).isEmpty();
		verifyNoInteractions(platformService);
	}

	private void givenQuestions(List<MemberQuestion> questions) {
		when(memberQuestionRetriever.findAllQuestions(RECEIVER_ID, 0, 10)).thenReturn(questions);
		when(memberQuestionRetriever.countAllQuestions(RECEIVER_ID)).thenReturn((long) questions.size());
	}

	private MemberQuestion question(Long id, Member asker, boolean isAnonymous) {
		MemberQuestion question = MemberQuestion.builder()
			.id(id)
			.receiver(receiver)
			.asker(asker)
			.content("질문" + id)
			.isAnonymous(isAnonymous)
			.build();
		ReflectionTestUtils.setField(question, "createdAt", LocalDateTime.now());
		return question;
	}

	private void answer(MemberQuestion question) {
		MemberAnswer answer = MemberAnswer.builder()
			.id(question.getId() * 10)
			.question(question)
			.content("답변")
			.build();
		ReflectionTestUtils.setField(answer, "createdAt", LocalDateTime.now());
		ReflectionTestUtils.setField(question, "answer", answer);
	}

	private InternalUserDetails userDetails(Long userId, String name, int generation) {
		return new InternalUserDetails(
			userId,
			name,
			"image" + userId,
			null,
			null,
			null,
			generation,
			List.of(new SoptActivity(1, generation, "서버", null, null, true))
		);
	}
}
