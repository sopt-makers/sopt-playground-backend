package org.sopt.makers.internal.sopmodam.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.makers.internal.exception.BadRequestException;
import org.sopt.makers.internal.exception.NotFoundException;
import org.sopt.makers.internal.sopmodam.domain.SopmodamQuestion;
import org.sopt.makers.internal.sopmodam.domain.SopmodamRound;
import org.sopt.makers.internal.sopmodam.domain.enums.SopmodamPhase;
import org.sopt.makers.internal.sopmodam.dto.request.SopmodamQuestionCreateRequest;
import org.sopt.makers.internal.sopmodam.dto.request.SopmodamQuestionUpdateRequest;
import org.sopt.makers.internal.sopmodam.dto.response.SopmodamQuestionResultResponse;
import org.sopt.makers.internal.sopmodam.dto.response.SopmodamRoundDetailResponse;
import org.sopt.makers.internal.external.platform.PlatformService;
import org.sopt.makers.internal.sopmodam.repository.SopmodamAnswerRepository;
import org.sopt.makers.internal.sopmodam.repository.SopmodamQuestionRepository;
import org.sopt.makers.internal.sopmodam.repository.SopmodamRoundRepository;

/**
 * 솝모담 어드민 질문 추가·수정·삭제 흐름 테스트.
 *
 * <p>검사 순서는 회차·질문 없음(404) → 투표 시작됨(400) 이고,
 * 실패하면 질문을 저장·삭제하지 않아야 한다.
 *
 * <p>리트리버·정책·쓰기 컴포넌트는 얇은 위임이라 실제 인스턴스를 쓰고, 리포지토리만 mock 으로 둔다.
 * 서비스는 {@code LocalDateTime.now(KST)} 를 직접 쓰므로, 회차 일정은 오늘(KST) 기준 상대 날짜로 만든다.
 */
@ExtendWith(MockitoExtension.class)
class SopmodamAdminServiceTest {

	private static final ZoneId KST = ZoneId.of("Asia/Seoul");
	private static final Long ROUND_ID = 1L;
	private static final Long QUESTION_ID = 10L;

	@Mock SopmodamRoundRepository roundRepository;
	@Mock SopmodamQuestionRepository questionRepository;
	@Mock SopmodamAnswerRepository answerRepository;
	@Mock PlatformService platformService;

	private SopmodamAdminService sopmodamAdminService;
	private LocalDate today;

	@BeforeEach
	void setUp() {
		sopmodamAdminService = new SopmodamAdminService(
			new SopmodamRoundRetriever(roundRepository),
			new SopmodamQuestionRetriever(questionRepository),
			new SopmodamAnswerRetriever(answerRepository),
			new SopmodamRoundModifier(roundRepository, questionRepository),
			new SopmodamQuestionModifier(questionRepository),
			new SopmodamRoundPolicy(),
			platformService
		);
		today = LocalDate.now(KST);
	}

	@Nested
	@DisplayName("질문 추가")
	class AddQuestion {

		@Test
		@DisplayName("투표 시작 전이면 질문을 저장하고 새 질문이 포함된 회차 상세를 돌려준다")
		void 투표_시작_전이면_추가() {
			SopmodamRound round = roundStartingIn(1);
			when(roundRepository.findById(ROUND_ID)).thenReturn(Optional.of(round));
			when(questionRepository.findAllByRoundIdOrderByIdAsc(ROUND_ID)).thenReturn(List.of(
				question(10L, round, "기존 질문 1"),
				question(11L, round, "기존 질문 2"),
				question(12L, round, "새 질문")
			));
			when(answerRepository.countByRoundId(ROUND_ID)).thenReturn(0L);

			SopmodamRoundDetailResponse response =
				sopmodamAdminService.addQuestion(ROUND_ID, new SopmodamQuestionCreateRequest("새 질문"));

			@SuppressWarnings("unchecked")
			ArgumentCaptor<List<SopmodamQuestion>> captor = ArgumentCaptor.forClass(List.class);
			verify(questionRepository).saveAll(captor.capture());
			assertThat(captor.getValue()).singleElement().satisfies(saved -> {
				assertThat(saved.getRound()).isSameAs(round);
				assertThat(saved.getContent()).isEqualTo("새 질문");
			});

			assertThat(response.roundId()).isEqualTo(ROUND_ID);
			assertThat(response.phase()).isEqualTo(SopmodamPhase.SCHEDULED);
			assertThat(response.questions())
				.extracting(SopmodamQuestionResultResponse::content)
				.containsExactly("기존 질문 1", "기존 질문 2", "새 질문");
		}

		@Test
		@DisplayName("투표가 시작된 회차면 400 이고 질문을 저장하지 않는다")
		void 투표_시작_후면_400() {
			when(roundRepository.findById(ROUND_ID)).thenReturn(Optional.of(roundStartingIn(0)));

			assertThatThrownBy(() ->
				sopmodamAdminService.addQuestion(ROUND_ID, new SopmodamQuestionCreateRequest("새 질문")))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining("투표가 시작된 회차의 질문은 변경할 수 없습니다.");
			verify(questionRepository, never()).saveAll(anyList());
		}

		@Test
		@DisplayName("회차가 없으면 404 이고 질문을 저장하지 않는다")
		void 회차_없으면_404() {
			when(roundRepository.findById(ROUND_ID)).thenReturn(Optional.empty());

			assertThatThrownBy(() ->
				sopmodamAdminService.addQuestion(ROUND_ID, new SopmodamQuestionCreateRequest("새 질문")))
				.isInstanceOf(NotFoundException.class)
				.hasMessageContaining("존재하지 않는 솝모담 회차입니다.");
			verify(questionRepository, never()).saveAll(anyList());
		}
	}

	@Nested
	@DisplayName("질문 수정")
	class UpdateQuestion {

		@Test
		@DisplayName("투표 시작 전이면 질문 내용을 바꾸고 질문이 속한 회차 상세를 돌려준다")
		void 투표_시작_전이면_수정() {
			SopmodamRound round = roundStartingIn(1);
			SopmodamQuestion question = question(QUESTION_ID, round, "기존 질문");
			when(questionRepository.findById(QUESTION_ID)).thenReturn(Optional.of(question));
			when(questionRepository.findAllByRoundIdOrderByIdAsc(ROUND_ID)).thenReturn(List.of(
				question,
				question(11L, round, "다른 질문")
			));
			when(answerRepository.countByRoundId(ROUND_ID)).thenReturn(0L);

			SopmodamRoundDetailResponse response =
				sopmodamAdminService.updateQuestion(QUESTION_ID, new SopmodamQuestionUpdateRequest("수정한 질문"));

			verify(questionRepository).save(question);
			assertThat(question.getContent()).isEqualTo("수정한 질문");

			assertThat(response.roundId()).isEqualTo(ROUND_ID);
			assertThat(response.phase()).isEqualTo(SopmodamPhase.SCHEDULED);
			assertThat(response.questions())
				.extracting(SopmodamQuestionResultResponse::content)
				.containsExactly("수정한 질문", "다른 질문");
		}

		@Test
		@DisplayName("투표가 시작된 회차의 질문이면 400 이고 질문을 바꾸지 않는다")
		void 투표_시작_후면_400() {
			SopmodamQuestion question = question(QUESTION_ID, roundStartingIn(0), "기존 질문");
			when(questionRepository.findById(QUESTION_ID)).thenReturn(Optional.of(question));

			assertThatThrownBy(() ->
				sopmodamAdminService.updateQuestion(QUESTION_ID, new SopmodamQuestionUpdateRequest("수정한 질문")))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining("투표가 시작된 회차의 질문은 변경할 수 없습니다.");
			assertThat(question.getContent()).isEqualTo("기존 질문");
			verify(questionRepository, never()).save(any());
		}

		@Test
		@DisplayName("질문이 없으면 404 이고 질문을 저장하지 않는다")
		void 질문_없으면_404() {
			when(questionRepository.findById(QUESTION_ID)).thenReturn(Optional.empty());

			assertThatThrownBy(() ->
				sopmodamAdminService.updateQuestion(QUESTION_ID, new SopmodamQuestionUpdateRequest("수정한 질문")))
				.isInstanceOf(NotFoundException.class)
				.hasMessageContaining("존재하지 않는 질문입니다.");
			verify(questionRepository, never()).save(any());
		}
	}

	@Nested
	@DisplayName("질문 삭제")
	class DeleteQuestion {

		@Test
		@DisplayName("투표 시작 전이고 질문이 3개 이상이면 질문을 삭제하고 남은 질문의 회차 상세를 돌려준다")
		void 투표_시작_전이면_삭제() {
			SopmodamRound round = roundStartingIn(1);
			SopmodamQuestion question = question(QUESTION_ID, round, "삭제할 질문");
			when(questionRepository.findById(QUESTION_ID)).thenReturn(Optional.of(question));
			when(questionRepository.findAllByRoundIdOrderByIdAsc(ROUND_ID)).thenReturn(List.of(
				question(11L, round, "남는 질문 1"),
				question(12L, round, "남는 질문 2")
			));
			when(answerRepository.countByRoundId(ROUND_ID)).thenReturn(0L);

			SopmodamRoundDetailResponse response = sopmodamAdminService.deleteQuestion(QUESTION_ID);

			verify(questionRepository).delete(question);

			assertThat(response.roundId()).isEqualTo(ROUND_ID);
			assertThat(response.phase()).isEqualTo(SopmodamPhase.SCHEDULED);
			assertThat(response.questions())
				.extracting(SopmodamQuestionResultResponse::content)
				.containsExactly("남는 질문 1", "남는 질문 2");
		}

		@Test
		@DisplayName("투표가 시작된 회차의 질문이면 400 이고 질문을 삭제하지 않는다")
		void 투표_시작_후면_400() {
			SopmodamQuestion question = question(QUESTION_ID, roundStartingIn(0), "삭제할 질문");
			when(questionRepository.findById(QUESTION_ID)).thenReturn(Optional.of(question));

			assertThatThrownBy(() -> sopmodamAdminService.deleteQuestion(QUESTION_ID))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining("투표가 시작된 회차의 질문은 변경할 수 없습니다.");
			verify(questionRepository, never()).delete(any());
		}

		@Test
		@DisplayName("회차에 질문이 2개뿐이어도 삭제하고 남은 질문을 돌려준다")
		void 질문_2개여도_삭제() {
			SopmodamRound round = roundStartingIn(1);
			SopmodamQuestion question = question(QUESTION_ID, round, "삭제할 질문");
			when(questionRepository.findById(QUESTION_ID)).thenReturn(Optional.of(question));
			when(questionRepository.findAllByRoundIdOrderByIdAsc(ROUND_ID))
				.thenReturn(List.of(question(11L, round, "남는 질문")));
			when(answerRepository.countByRoundId(ROUND_ID)).thenReturn(0L);

			SopmodamRoundDetailResponse response = sopmodamAdminService.deleteQuestion(QUESTION_ID);

			verify(questionRepository).delete(question);
			assertThat(response.roundId()).isEqualTo(ROUND_ID);
			assertThat(response.questions())
				.extracting(SopmodamQuestionResultResponse::content)
				.containsExactly("남는 질문");
		}

		@Test
		@DisplayName("마지막 질문도 삭제하고 빈 질문 목록을 돌려준다")
		void 마지막_질문도_삭제() {
			SopmodamRound round = roundStartingIn(1);
			SopmodamQuestion question = question(QUESTION_ID, round, "삭제할 질문");
			when(questionRepository.findById(QUESTION_ID)).thenReturn(Optional.of(question));
			when(questionRepository.findAllByRoundIdOrderByIdAsc(ROUND_ID)).thenReturn(List.of());
			when(answerRepository.countByRoundId(ROUND_ID)).thenReturn(0L);

			SopmodamRoundDetailResponse response = sopmodamAdminService.deleteQuestion(QUESTION_ID);

			verify(questionRepository).delete(question);
			assertThat(response.roundId()).isEqualTo(ROUND_ID);
			assertThat(response.phase()).isEqualTo(SopmodamPhase.SCHEDULED);
			assertThat(response.questions()).isEmpty();
		}

		@Test
		@DisplayName("질문이 없으면 404 이고 질문을 삭제하지 않는다")
		void 질문_없으면_404() {
			when(questionRepository.findById(QUESTION_ID)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> sopmodamAdminService.deleteQuestion(QUESTION_ID))
				.isInstanceOf(NotFoundException.class)
				.hasMessageContaining("존재하지 않는 질문입니다.");
			verify(questionRepository, never()).delete(any());
		}
	}

	// 오늘(KST)로부터 days 일 뒤 00:00 에 투표가 시작되는 회차. 0 이면 오늘 00:00 에 이미 시작됐다
	private SopmodamRound roundStartingIn(int days) {
		LocalDateTime voteStartAt = today.plusDays(days).atStartOfDay();
		return SopmodamRound.builder()
			.id(ROUND_ID)
			.generation(37)
			.eventName("SOPT 첫 협업")
			.voteStartAt(voteStartAt)
			.voteEndAt(voteStartAt.plusDays(5))
			.answerEndAt(voteStartAt.plusDays(26))
			.build();
	}

	private SopmodamQuestion question(Long id, SopmodamRound round, String content) {
		return SopmodamQuestion.builder().id(id).round(round).content(content).build();
	}
}
