package org.sopt.makers.internal.sopmodam.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.time.LocalDateTime;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.makers.internal.exception.ConflictException;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.sopmodam.domain.SopmodamAnswer;
import org.sopt.makers.internal.sopmodam.domain.SopmodamQuestion;
import org.sopt.makers.internal.sopmodam.domain.SopmodamRound;
import org.sopt.makers.internal.sopmodam.repository.SopmodamAnswerRepository;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * 답변 저장 테스트.
 *
 * <p>서비스의 중복 검사와 저장 사이에는 틈이 있어, 같은 사용자의 동시 요청은
 * DB 의 {@code (question_id, member_id)} 유니크 제약에서 걸린다.
 * 그 위반이 500 이 아니라 409 로 나가야 한다.
 */
@ExtendWith(MockitoExtension.class)
class SopmodamAnswerModifierTest {

	@Mock SopmodamAnswerRepository answerRepository;

	@InjectMocks SopmodamAnswerModifier answerModifier;

	private final SopmodamRound round = SopmodamRound.builder()
		.id(1L)
		.generation(37)
		.eventName("SOPT 첫 협업")
		.voteStartAt(LocalDateTime.of(2026, 10, 10, 0, 0))
		.voteEndAt(LocalDateTime.of(2026, 10, 15, 0, 0))
		.answerEndAt(LocalDateTime.of(2026, 11, 5, 0, 0))
		.build();
	private final SopmodamQuestion question = SopmodamQuestion.builder()
		.id(10L).round(round).content("선정된 질문").isSelected(true).build();
	private final Member member = Member.builder().id(100L).build();
	private static final String CONTENT = "솝트에서 배운 협업 경험을 바탕으로 말씀드리겠습니다.";

	@Test
	@DisplayName("질문·회원·내용을 담아 답변을 저장한다")
	void 답변_저장() {
		SopmodamAnswer saved = SopmodamAnswer.builder()
			.question(question).member(member).content(CONTENT).build();
		when(answerRepository.saveAndFlush(any(SopmodamAnswer.class))).thenReturn(saved);

		SopmodamAnswer result = answerModifier.createAnswer(question, member, CONTENT);

		ArgumentCaptor<SopmodamAnswer> captor = ArgumentCaptor.forClass(SopmodamAnswer.class);
		verify(answerRepository).saveAndFlush(captor.capture());

		SopmodamAnswer captured = captor.getValue();
		assertThat(captured.getQuestion()).isSameAs(question);
		assertThat(captured.getMember()).isSameAs(member);
		assertThat(captured.getContent()).isEqualTo(CONTENT);
		assertThat(result).isSameAs(saved);
	}

	@Test
	@DisplayName("동시 요청으로 유니크 제약에 걸리면 409 로 바꾼다")
	void 유니크_제약_위반이면_409() {
		when(answerRepository.saveAndFlush(any(SopmodamAnswer.class)))
			.thenThrow(new DataIntegrityViolationException("duplicate", new ConstraintViolationException(
				"duplicate", new SQLException("duplicate", "23505"), "uk_sopmodam_answer_question_member")));

		assertThatThrownBy(() -> answerModifier.createAnswer(question, member, CONTENT))
			.isInstanceOf(ConflictException.class)
			.hasMessageContaining("이미 답변하셨습니다.");
	}

	@Test
	@DisplayName("기존 답변의 내용을 수정하고 저장한다")
	void 답변_수정() {
		SopmodamAnswer answer = SopmodamAnswer.builder()
			.question(question).member(member).content(CONTENT).build();
		String newContent = "수정된 답변입니다. 공백 제외 이십 자 이상이어야 합니다요.";
		when(answerRepository.save(answer)).thenReturn(answer);

		SopmodamAnswer result = answerModifier.updateAnswer(answer, newContent);

		verify(answerRepository).save(answer);
		assertThat(result.getContent()).isEqualTo(newContent);
	}

	@Test
	@DisplayName("다른 종류의 DB 제약 위반은 그대로 재던진다")
	void 다른_제약_위반은_재던진다() {
		DataIntegrityViolationException otherViolation = new DataIntegrityViolationException(
			"other", new ConstraintViolationException(
				"other", new SQLException("other", "23505"), "uk_other_constraint"));
		when(answerRepository.saveAndFlush(any(SopmodamAnswer.class))).thenThrow(otherViolation);

		assertThatThrownBy(() -> answerModifier.createAnswer(question, member, CONTENT))
			.isInstanceOf(DataIntegrityViolationException.class);
	}
}
