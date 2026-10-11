package org.sopt.makers.internal.sopmodam.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.makers.internal.exception.BadRequestException;
import org.sopt.makers.internal.exception.ConflictException;
import org.sopt.makers.internal.exception.ForbiddenException;
import org.sopt.makers.internal.exception.NotFoundException;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.external.platform.PlatformService;
import org.sopt.makers.internal.external.platform.SoptActivity;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.service.MemberRetriever;
import org.sopt.makers.internal.sopmodam.domain.SopmodamQuestion;
import org.sopt.makers.internal.sopmodam.domain.SopmodamRound;
import org.sopt.makers.internal.sopmodam.dto.response.SopmodamQuestionListResponse;
import org.sopt.makers.internal.sopmodam.dto.response.SopmodamQuestionResponse;
import org.sopt.makers.internal.sopmodam.dto.response.SopmodamVoteResponse;
import org.sopt.makers.internal.sopmodam.repository.SopmodamQuestionRepository;
import org.sopt.makers.internal.sopmodam.repository.SopmodamQuestionVoteRepository;
import org.sopt.makers.internal.sopmodam.repository.SopmodamRoundRepository;

/**
 * 솝모담 투표 화면 조회(U2)·질문 투표(U3) 흐름 테스트.
 *
 * <p>검사 순서가 응답 코드를 결정하므로 순서 자체를 검증한다.
 * <pre>
 *   회차 없음(404) → 투표 대상 아님(403) → 투표 기간 아님(400) → 회차 소속 질문 아님(404) → 이미 투표(409)
 * </pre>
 * 앞 단계에서 실패하면 뒤 단계의 조회·저장이 일어나지 않아야 한다.
 *
 * <p>리트리버·정책은 얇은 위임이라 실제 인스턴스를 쓰고, 리포지토리·Platform·쓰기 컴포넌트만 mock 으로 둔다.
 * 서비스는 {@code LocalDateTime.now(KST)} 를 직접 쓰므로, 회차 일정은 오늘(KST) 기준 상대 날짜로 만든다.
 */
@ExtendWith(MockitoExtension.class)
class SopmodamServiceTest {

	private static final ZoneId KST = ZoneId.of("Asia/Seoul");
	private static final Long ROUND_ID = 1L;
	private static final Long USER_ID = 100L;
	private static final int ROUND_GENERATION = 37;

	@Mock SopmodamRoundRepository roundRepository;
	@Mock SopmodamQuestionRepository questionRepository;
	@Mock SopmodamQuestionVoteRepository questionVoteRepository;
	@Mock SopmodamVoteModifier voteModifier;
	@Mock MemberRetriever memberRetriever;
	@Mock PlatformService platformService;

	private SopmodamService sopmodamService;
	private LocalDate today;

	@BeforeEach
	void setUp() {
		sopmodamService = new SopmodamService(
			new SopmodamRoundRetriever(roundRepository),
			new SopmodamQuestionRetriever(questionRepository),
			new SopmodamVoteRetriever(questionVoteRepository),
			voteModifier,
			new SopmodamMemberPolicy(),
			memberRetriever,
			platformService
		);
		today = LocalDate.now(KST);
	}

	@Nested
	@DisplayName("U2 투표 화면 조회")
	class GetQuestions {

		@Test
		@DisplayName("회차가 없으면 404 이고 Platform 을 호출하지 않는다")
		void 회차가_없으면_404() {
			when(roundRepository.findById(ROUND_ID)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> sopmodamService.getQuestions(USER_ID, ROUND_ID))
				.isInstanceOf(NotFoundException.class)
				.hasMessageContaining("존재하지 않는 솝모담 회차입니다.");
			verifyNoInteractions(platformService, questionRepository, questionVoteRepository);
		}

		@Test
		@DisplayName("명예기수는 403 이고 질문·투표를 조회하지 않는다")
		void 명예기수는_403() {
			givenRound(votingRound());
			givenUser(honorary());

			assertThatThrownBy(() -> sopmodamService.getQuestions(USER_ID, ROUND_ID))
				.isInstanceOf(ForbiddenException.class)
				.hasMessageContaining("질문 투표는 활동 기수만 참여할 수 있습니다.");
			verifyNoInteractions(questionRepository, questionVoteRepository);
		}

		@Test
		@DisplayName("회차 기수보다 뒤 기수인 사용자는 403")
		void 뒤_기수_사용자는_403() {
			givenRound(votingRound());
			givenUser(laterGeneration());

			assertThatThrownBy(() -> sopmodamService.getQuestions(USER_ID, ROUND_ID))
				.isInstanceOf(ForbiddenException.class);
		}

		@Test
		@DisplayName("명예기수가 시작 전 회차를 조회하면 기간(400)보다 권한(403)이 먼저다")
		void 명예기수_시작_전_회차는_403이_먼저() {
			givenRound(scheduledRound());
			givenUser(honorary());

			assertThatThrownBy(() -> sopmodamService.getQuestions(USER_ID, ROUND_ID))
				.isInstanceOf(ForbiddenException.class);
		}

		@Test
		@DisplayName("투표 시작 전 회차는 400 이고 질문을 노출하지 않는다")
		void 투표_시작_전이면_400() {
			givenRound(scheduledRound());
			givenUser(activeYb());

			assertThatThrownBy(() -> sopmodamService.getQuestions(USER_ID, ROUND_ID))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining("투표 기간이 아닙니다.");
			verifyNoInteractions(questionRepository, questionVoteRepository);
		}

		@Test
		@DisplayName("투표 중이고 아직 투표하지 않았으면 투표할 수 있다")
		void 투표_중_투표_전() {
			SopmodamRound round = votingRound();
			givenRound(round);
			givenUser(activeYb());
			givenQuestions(round);
			when(questionVoteRepository.findQuestionIdByRoundIdAndMemberId(ROUND_ID, USER_ID)).thenReturn(Optional.empty());

			SopmodamQuestionListResponse response = sopmodamService.getQuestions(USER_ID, ROUND_ID);

			assertThat(response.roundId()).isEqualTo(ROUND_ID);
			assertThat(response.eventName()).isEqualTo("SOPT 첫 협업");
			assertThat(response.voteEndAt()).isEqualTo(round.getVoteEndAt());
			assertThat(response.dDay()).isEqualTo(4);
			assertThat(response.canVote()).isTrue();
			assertThat(response.hasVoted()).isFalse();
			assertThat(response.myVoteQuestionId()).isNull();
			assertThat(response.questions()).containsExactly(
				new SopmodamQuestionResponse(1L, "질문 1"),
				new SopmodamQuestionResponse(2L, "질문 2")
			);
		}

		@Test
		@DisplayName("활동 OB 도 조회할 수 있다")
		void 활동_OB도_조회_가능() {
			SopmodamRound round = votingRound();
			givenRound(round);
			givenUser(activeOb());
			givenQuestions(round);
			when(questionVoteRepository.findQuestionIdByRoundIdAndMemberId(ROUND_ID, USER_ID)).thenReturn(Optional.empty());

			assertThat(sopmodamService.getQuestions(USER_ID, ROUND_ID).canVote()).isTrue();
		}

		@Test
		@DisplayName("투표 중이어도 이미 투표했으면 투표할 수 없고, 내가 고른 질문을 내려준다")
		void 투표_중_투표_후() {
			SopmodamRound round = votingRound();
			givenRound(round);
			givenUser(activeYb());
			givenQuestions(round);
			when(questionVoteRepository.findQuestionIdByRoundIdAndMemberId(ROUND_ID, USER_ID)).thenReturn(Optional.of(2L));

			SopmodamQuestionListResponse response = sopmodamService.getQuestions(USER_ID, ROUND_ID);

			assertThat(response.canVote()).isFalse();
			assertThat(response.hasVoted()).isTrue();
			assertThat(response.myVoteQuestionId()).isEqualTo(2L);
			assertThat(response.dDay()).isEqualTo(4);
		}

		@Test
		@DisplayName("투표 마감이 내일 0시면 dDay 는 0 (D-Day)")
		void 마감이_내일_0시면_D_Day() {
			SopmodamRound round = round(today.minusDays(1), today.plusDays(1), today.plusDays(20));
			givenRound(round);
			givenUser(activeYb());
			givenQuestions(round);
			when(questionVoteRepository.findQuestionIdByRoundIdAndMemberId(ROUND_ID, USER_ID)).thenReturn(Optional.empty());

			assertThat(sopmodamService.getQuestions(USER_ID, ROUND_ID).dDay()).isZero();
		}

		@Test
		@DisplayName("투표가 끝난 뒤(답변 모집 중)에도 200 이고, 투표 불가·dDay null 이다")
		void 투표_마감_후_답변_모집_중() {
			SopmodamRound round = answeringRound();
			givenRound(round);
			givenUser(activeYb());
			givenQuestions(round);
			when(questionVoteRepository.findQuestionIdByRoundIdAndMemberId(ROUND_ID, USER_ID)).thenReturn(Optional.empty());

			SopmodamQuestionListResponse response = sopmodamService.getQuestions(USER_ID, ROUND_ID);

			assertThat(response.canVote()).isFalse();
			assertThat(response.hasVoted()).isFalse();
			assertThat(response.dDay()).isNull();
			assertThat(response.questions()).hasSize(2);
		}

		@Test
		@DisplayName("답변 공개 단계에서도 조회되고, 내 투표 기록이 유지된다")
		void 답변_공개_단계() {
			SopmodamRound round = revealedRound();
			givenRound(round);
			givenUser(activeOb());
			givenQuestions(round);
			when(questionVoteRepository.findQuestionIdByRoundIdAndMemberId(ROUND_ID, USER_ID)).thenReturn(Optional.of(1L));

			SopmodamQuestionListResponse response = sopmodamService.getQuestions(USER_ID, ROUND_ID);

			assertThat(response.canVote()).isFalse();
			assertThat(response.hasVoted()).isTrue();
			assertThat(response.myVoteQuestionId()).isEqualTo(1L);
			assertThat(response.dDay()).isNull();
		}
	}

	@Nested
	@DisplayName("U3 질문 투표")
	class Vote {

		private static final Long QUESTION_ID = 2L;

		@Test
		@DisplayName("활동 기수가 투표 기간에 회차 소속 질문에 처음 투표하면 저장한다")
		void 정상_투표() {
			SopmodamRound round = votingRound();
			SopmodamQuestion question = question(QUESTION_ID, round);
			Member member = Member.builder().id(USER_ID).build();
			givenRound(round);
			givenUser(activeYb());
			when(questionRepository.findByIdAndRoundId(QUESTION_ID, ROUND_ID)).thenReturn(Optional.of(question));
			when(questionVoteRepository.existsByRoundIdAndMemberId(ROUND_ID, USER_ID)).thenReturn(false);
			when(memberRetriever.findMemberById(USER_ID)).thenReturn(member);

			SopmodamVoteResponse response = sopmodamService.vote(USER_ID, ROUND_ID, QUESTION_ID);

			assertThat(response).isEqualTo(new SopmodamVoteResponse(ROUND_ID, QUESTION_ID));
			verify(voteModifier).createVote(round, question, member);
		}

		@Test
		@DisplayName("회차가 없으면 404 이고 Platform 을 호출하지 않는다")
		void 회차가_없으면_404() {
			when(roundRepository.findById(ROUND_ID)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> sopmodamService.vote(USER_ID, ROUND_ID, QUESTION_ID))
				.isInstanceOf(NotFoundException.class)
				.hasMessageContaining("존재하지 않는 솝모담 회차입니다.");
			verifyNoInteractions(platformService, voteModifier);
		}

		@Test
		@DisplayName("명예기수는 403 이고 질문을 조회하지 않는다")
		void 명예기수는_403() {
			givenRound(votingRound());
			givenUser(honorary());

			assertThatThrownBy(() -> sopmodamService.vote(USER_ID, ROUND_ID, QUESTION_ID))
				.isInstanceOf(ForbiddenException.class)
				.hasMessageContaining("질문 투표는 활동 기수만 참여할 수 있습니다.");
			verifyNoInteractions(questionRepository, questionVoteRepository, voteModifier);
		}

		@Test
		@DisplayName("회차 기수보다 뒤 기수인 사용자는 403")
		void 뒤_기수_사용자는_403() {
			givenRound(votingRound());
			givenUser(laterGeneration());

			assertThatThrownBy(() -> sopmodamService.vote(USER_ID, ROUND_ID, QUESTION_ID))
				.isInstanceOf(ForbiddenException.class);
			verifyNoInteractions(voteModifier);
		}

		@Test
		@DisplayName("명예기수가 마감된 회차에 투표하면 기간(400)보다 권한(403)이 먼저다")
		void 명예기수_마감_회차는_403이_먼저() {
			givenRound(answeringRound());
			givenUser(honorary());

			assertThatThrownBy(() -> sopmodamService.vote(USER_ID, ROUND_ID, QUESTION_ID))
				.isInstanceOf(ForbiddenException.class);
		}

		@Test
		@DisplayName("투표 시작 전이면 400 이고 질문을 조회하지 않는다")
		void 투표_시작_전이면_400() {
			givenRound(scheduledRound());
			givenUser(activeYb());

			assertThatThrownBy(() -> sopmodamService.vote(USER_ID, ROUND_ID, QUESTION_ID))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining("투표 기간이 아닙니다.");
			verifyNoInteractions(questionRepository, questionVoteRepository, voteModifier);
		}

		@Test
		@DisplayName("투표가 끝났으면 400")
		void 투표_마감_후면_400() {
			givenRound(answeringRound());
			givenUser(activeOb());

			assertThatThrownBy(() -> sopmodamService.vote(USER_ID, ROUND_ID, QUESTION_ID))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining("투표 기간이 아닙니다.");
			verifyNoInteractions(voteModifier);
		}

		@Test
		@DisplayName("답변 공개 단계에서도 400")
		void 답변_공개_단계면_400() {
			givenRound(revealedRound());
			givenUser(activeYb());

			assertThatThrownBy(() -> sopmodamService.vote(USER_ID, ROUND_ID, QUESTION_ID))
				.isInstanceOf(BadRequestException.class);
			verifyNoInteractions(voteModifier);
		}

		@Test
		@DisplayName("질문이 없거나 다른 회차의 질문이면 404 이고 중복 투표 검사로 넘어가지 않는다")
		void 회차_소속_질문이_아니면_404() {
			givenRound(votingRound());
			givenUser(activeYb());
			when(questionRepository.findByIdAndRoundId(QUESTION_ID, ROUND_ID)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> sopmodamService.vote(USER_ID, ROUND_ID, QUESTION_ID))
				.isInstanceOf(NotFoundException.class)
				.hasMessageContaining("해당 회차에 존재하지 않는 질문입니다.");
			verifyNoInteractions(questionVoteRepository, voteModifier);
		}

		@Test
		@DisplayName("이번 회차에 이미 투표했으면 다른 질문이어도 409 이고 저장하지 않는다")
		void 이미_투표했으면_409() {
			SopmodamRound round = votingRound();
			givenRound(round);
			givenUser(activeYb());
			when(questionRepository.findByIdAndRoundId(QUESTION_ID, ROUND_ID)).thenReturn(Optional.of(question(QUESTION_ID, round)));
			when(questionVoteRepository.existsByRoundIdAndMemberId(ROUND_ID, USER_ID)).thenReturn(true);

			assertThatThrownBy(() -> sopmodamService.vote(USER_ID, ROUND_ID, QUESTION_ID))
				.isInstanceOf(ConflictException.class)
				.hasMessageContaining("이미 투표한 회차입니다.");
			verify(memberRetriever, never()).findMemberById(anyLong());
			verifyNoInteractions(voteModifier);
		}

		@Test
		@DisplayName("플레이그라운드 회원이 없으면 저장 전에 404 로 끝난다 (FK 위반이 409 로 바뀌지 않도록)")
		void 회원이_없으면_저장하지_않는다() {
			SopmodamRound round = votingRound();
			givenRound(round);
			givenUser(activeYb());
			when(questionRepository.findByIdAndRoundId(QUESTION_ID, ROUND_ID)).thenReturn(Optional.of(question(QUESTION_ID, round)));
			when(questionVoteRepository.existsByRoundIdAndMemberId(ROUND_ID, USER_ID)).thenReturn(false);
			when(memberRetriever.findMemberById(USER_ID)).thenThrow(new NotFoundException("존재하지 않는 사용자의 id값 입니다."));

			assertThatThrownBy(() -> sopmodamService.vote(USER_ID, ROUND_ID, QUESTION_ID))
				.isInstanceOf(NotFoundException.class);
			verify(voteModifier, never()).createVote(any(), any(), any());
		}
	}

	private void givenRound(SopmodamRound round) {
		when(roundRepository.findById(ROUND_ID)).thenReturn(Optional.of(round));
	}

	private void givenUser(InternalUserDetails user) {
		when(platformService.getInternalUser(USER_ID)).thenReturn(user);
	}

	private void givenQuestions(SopmodamRound round) {
		when(questionRepository.findAllByRoundIdOrderByIdAsc(ROUND_ID)).thenReturn(List.of(
			question(1L, round, "질문 1"),
			question(2L, round, "질문 2")
		));
	}

	private SopmodamRound scheduledRound() {
		return round(today.plusDays(1), today.plusDays(6), today.plusDays(20));
	}

	// 투표 마감이 5일 뒤 0시 → 오늘 기준 D-4
	private SopmodamRound votingRound() {
		return round(today.minusDays(1), today.plusDays(5), today.plusDays(20));
	}

	private SopmodamRound answeringRound() {
		return round(today.minusDays(10), today.minusDays(1), today.plusDays(10));
	}

	private SopmodamRound revealedRound() {
		return round(today.minusDays(30), today.minusDays(20), today.minusDays(1));
	}

	private SopmodamRound round(LocalDate voteStart, LocalDate voteEnd, LocalDate answerEnd) {
		return SopmodamRound.builder()
			.id(ROUND_ID)
			.generation(ROUND_GENERATION)
			.eventName("SOPT 첫 협업")
			.voteStartAt(voteStart.atStartOfDay())
			.voteEndAt(voteEnd.atStartOfDay())
			.answerEndAt(answerEnd.atStartOfDay())
			.build();
	}

	private SopmodamQuestion question(Long questionId, SopmodamRound round) {
		return question(questionId, round, "질문 " + questionId);
	}

	private SopmodamQuestion question(Long questionId, SopmodamRound round, String content) {
		return SopmodamQuestion.builder().id(questionId).round(round).content(content).build();
	}

	private InternalUserDetails activeYb() {
		return user(ROUND_GENERATION, sopt(ROUND_GENERATION));
	}

	private InternalUserDetails activeOb() {
		return user(ROUND_GENERATION, sopt(ROUND_GENERATION - 2), sopt(ROUND_GENERATION));
	}

	private InternalUserDetails honorary() {
		return user(ROUND_GENERATION - 1, sopt(ROUND_GENERATION - 1));
	}

	private InternalUserDetails laterGeneration() {
		return user(ROUND_GENERATION + 1, sopt(ROUND_GENERATION + 1));
	}

	private InternalUserDetails user(int lastGeneration, SoptActivity... activities) {
		return new InternalUserDetails(USER_ID, "홍길동", null, null, null, null, lastGeneration, List.of(activities));
	}

	private SoptActivity sopt(int generation) {
		return new SoptActivity(generation, generation, "서버", null, "MEMBER", true);
	}
}
