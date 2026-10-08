package org.sopt.makers.internal.sopmodam.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.makers.internal.exception.ConflictException;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.sopmodam.domain.SopmodamQuestion;
import org.sopt.makers.internal.sopmodam.domain.SopmodamQuestionVote;
import org.sopt.makers.internal.sopmodam.domain.SopmodamRound;
import org.sopt.makers.internal.sopmodam.repository.SopmodamQuestionRepository;
import org.sopt.makers.internal.sopmodam.repository.SopmodamQuestionVoteRepository;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * 투표 저장 테스트.
 *
 * <p>서비스의 중복 투표 검사(exists)와 저장 사이에는 틈이 있어, 같은 사용자의 동시 요청은
 * DB 의 {@code (round_id, member_id)} 유니크 제약에서 걸린다. 그 위반이 500 이 아니라 409 로 나가야 하고,
 * 득표수는 올라가지 않아야 한다.
 *
 * <p>트랜잭션 롤백 자체는 프록시가 없는 단위 테스트로 검증할 수 없다.
 */
@ExtendWith(MockitoExtension.class)
class SopmodamVoteModifierTest {

	@Mock SopmodamQuestionVoteRepository questionVoteRepository;
	@Mock SopmodamQuestionRepository questionRepository;

	@InjectMocks SopmodamVoteModifier voteModifier;

	private final SopmodamRound round = SopmodamRound.builder()
		.id(1L)
		.generation(37)
		.eventName("SOPT 첫 협업")
		.voteStartAt(LocalDateTime.of(2026, 10, 10, 0, 0))
		.voteEndAt(LocalDateTime.of(2026, 10, 15, 0, 0))
		.answerEndAt(LocalDateTime.of(2026, 11, 5, 0, 0))
		.build();
	private final SopmodamQuestion question = SopmodamQuestion.builder().id(2L).round(round).content("질문").build();
	private final Member member = Member.builder().id(100L).build();

	@Test
	@DisplayName("회차·질문·회원을 담아 투표를 저장한 뒤 득표수를 올린다")
	void 투표_저장_후_득표수_증가() {
		voteModifier.createVote(round, question, member);

		ArgumentCaptor<SopmodamQuestionVote> captor = ArgumentCaptor.forClass(SopmodamQuestionVote.class);
		InOrder inOrder = inOrder(questionVoteRepository, questionRepository);
		inOrder.verify(questionVoteRepository).saveAndFlush(captor.capture());
		inOrder.verify(questionRepository).increaseVoteCount(2L);

		SopmodamQuestionVote saved = captor.getValue();
		assertThat(saved.getRound()).isSameAs(round);
		assertThat(saved.getQuestion()).isSameAs(question);
		assertThat(saved.getMember()).isSameAs(member);
	}

	@Test
	@DisplayName("동시 요청으로 유니크 제약에 걸리면 409 로 바꾸고 득표수를 올리지 않는다")
	void 유니크_제약_위반이면_409() {
		when(questionVoteRepository.saveAndFlush(any(SopmodamQuestionVote.class)))
			.thenThrow(new DataIntegrityViolationException("uk_sopmodam_question_vote_round_member"));

		assertThatThrownBy(() -> voteModifier.createVote(round, question, member))
			.isInstanceOf(ConflictException.class)
			.hasMessageContaining("이미 투표한 회차입니다.");
		verify(questionRepository, never()).increaseVoteCount(anyLong());
	}
}
