package org.sopt.makers.internal.sopmodam.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
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
import org.sopt.makers.internal.sopmodam.domain.SopmodamAnswer;
import org.sopt.makers.internal.sopmodam.domain.SopmodamQuestion;
import org.sopt.makers.internal.sopmodam.domain.SopmodamRound;
import org.sopt.makers.internal.sopmodam.domain.enums.SopmodamCardType;
import org.sopt.makers.internal.sopmodam.domain.enums.SopmodamPhase;
import org.sopt.makers.internal.sopmodam.dto.response.SopmodamCurrentCardResponse;
import org.sopt.makers.internal.sopmodam.dto.request.SopmodamAnswerCreateRequest;
import org.sopt.makers.internal.sopmodam.dto.request.SopmodamAnswerUpdateRequest;
import org.sopt.makers.internal.sopmodam.dto.response.SopmodamAnswerCreateResponse;
import org.sopt.makers.internal.sopmodam.dto.response.SopmodamAnswerUpdateResponse;
import org.sopt.makers.internal.sopmodam.dto.response.SopmodamQuestionListResponse;
import org.sopt.makers.internal.sopmodam.dto.response.SopmodamQuestionResponse;
import org.sopt.makers.internal.sopmodam.dto.response.SopmodamVoteResponse;
import org.sopt.makers.internal.sopmodam.repository.SopmodamAnswerRepository;
import org.sopt.makers.internal.sopmodam.repository.SopmodamQuestionRepository;
import org.sopt.makers.internal.sopmodam.repository.SopmodamQuestionVoteRepository;
import org.sopt.makers.internal.sopmodam.repository.SopmodamRoundRepository;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 솝모담 홈 카드 조회(U1)·투표 화면 조회(U2)·질문 투표(U3) 흐름 테스트.
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
	@Mock SopmodamAnswerRepository answerRepository;
	@Mock SopmodamVoteModifier voteModifier;
	@Mock SopmodamAnswerModifier answerModifier;
	@Mock MemberRetriever memberRetriever;
	@Mock PlatformService platformService;

	private SopmodamService sopmodamService;
	private LocalDate today;

	@BeforeEach
	void setUp() {
		sopmodamService = new SopmodamService(
			new SopmodamRoundRetriever(roundRepository),
			new SopmodamRoundModifier(roundRepository, questionRepository),
			new SopmodamQuestionRetriever(questionRepository),
			new SopmodamVoteRetriever(questionVoteRepository),
			new SopmodamAnswerRetriever(answerRepository),
			voteModifier,
			answerModifier,
			new SopmodamMemberPolicy(),
			new SopmodamRoundPolicy(),
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

	@Nested
	@DisplayName("U1 홈 카드 조회")
	class GetCurrentCard {

		@Test
		@DisplayName("현재 회차가 없으면 카드가 없고 Platform 을 호출하지 않는다")
		void 현재_회차가_없으면_카드_없음() {
			when(roundRepository.findFirstByVoteStartAtLessThanEqualOrderByVoteStartAtDesc(any(LocalDateTime.class)))
				.thenReturn(Optional.empty());

			assertThat(sopmodamService.getCurrentCard(USER_ID)).isEmpty();
			verifyNoInteractions(platformService);
		}

		@Test
		@DisplayName("투표 중인 회차의 명예기수는 카드가 없고 투표·질문을 조회하지 않는다")
		void 투표_중_명예기수는_카드_없음() {
			givenCurrentRound(votingRound());
			givenUser(honorary());

			assertThat(sopmodamService.getCurrentCard(USER_ID)).isEmpty();
			verifyNoInteractions(questionRepository, questionVoteRepository, answerRepository);
		}

		@Test
		@DisplayName("투표 중이고 아직 투표하지 않았으면 투표 카드에서 투표할 수 있다")
		void 투표_중_투표_전() {
			SopmodamRound round = votingRound();
			givenCurrentRound(round);
			givenUser(activeYb());
			when(questionVoteRepository.findQuestionIdByRoundIdAndMemberId(ROUND_ID, USER_ID)).thenReturn(Optional.empty());

			SopmodamCurrentCardResponse response = sopmodamService.getCurrentCard(USER_ID).orElseThrow();

			assertThat(response.roundId()).isEqualTo(ROUND_ID);
			assertThat(response.eventName()).isEqualTo("SOPT 첫 협업");
			assertThat(response.phase()).isEqualTo(SopmodamPhase.VOTING);
			assertThat(response.cardType()).isEqualTo(SopmodamCardType.VOTE);
			assertThat(response.voteEndAt()).isEqualTo(round.getVoteEndAt());
			assertThat(response.answerEndAt()).isEqualTo(round.getAnswerEndAt());
			assertThat(response.dDay()).isEqualTo(4);
			assertThat(response.selectedQuestion()).isNull();
			assertThat(response.canVote()).isTrue();
			assertThat(response.hasVoted()).isFalse();
			assertThat(response.canAnswer()).isFalse();
			assertThat(response.hasAnswered()).isFalse();
			assertThat(response.answerCount()).isNull();
			verifyNoInteractions(questionRepository);
			verify(answerRepository).existsByRoundIdAndMemberId(ROUND_ID, USER_ID);
		}

		@Test
		@DisplayName("활동 OB 가 이미 투표했으면 투표 카드에서 투표할 수 없다")
		void 투표_중_투표_후() {
			givenCurrentRound(votingRound());
			givenUser(activeOb());
			when(questionVoteRepository.findQuestionIdByRoundIdAndMemberId(ROUND_ID, USER_ID)).thenReturn(Optional.of(2L));

			SopmodamCurrentCardResponse response = sopmodamService.getCurrentCard(USER_ID).orElseThrow();

			assertThat(response.cardType()).isEqualTo(SopmodamCardType.VOTE);
			assertThat(response.canVote()).isFalse();
			assertThat(response.hasVoted()).isTrue();
		}

		@Test
		@DisplayName("답변 모집 중 활동 YB 는 답변 준비 중 카드이고, 답변 마감 기준 dDay 와 최종 질문을 내려준다")
		void 답변_모집_중_활동_YB() {
			SopmodamRound round = answeringRound();
			givenCurrentRound(round);
			givenUser(activeYb());
			givenFinalQuestion(round, question(2L, round, "질문 2", 5, false));
			when(questionVoteRepository.findQuestionIdByRoundIdAndMemberId(ROUND_ID, USER_ID)).thenReturn(Optional.of(2L));

			SopmodamCurrentCardResponse response = sopmodamService.getCurrentCard(USER_ID).orElseThrow();

			assertThat(response.phase()).isEqualTo(SopmodamPhase.ANSWERING);
			assertThat(response.cardType()).isEqualTo(SopmodamCardType.ANSWER_PREPARING);
			assertThat(response.dDay()).isEqualTo(9);
			assertThat(response.selectedQuestion()).isEqualTo(new SopmodamQuestionResponse(2L, "질문 2"));
			assertThat(response.canVote()).isFalse();
			assertThat(response.canAnswer()).isFalse();
			assertThat(response.hasAnswered()).isFalse();
			assertThat(response.answerCount()).isNull();
			assertThat(response.hasVoted()).isTrue();
			verify(answerRepository).existsByRoundIdAndMemberId(ROUND_ID, USER_ID);
		}

		@Test
		@DisplayName("답변 모집 중 활동 OB 가 아직 답변하지 않았으면 답변할 수 있다")
		void 답변_모집_중_활동_OB_답변_전() {
			SopmodamRound round = answeringRound();
			givenCurrentRound(round);
			givenUser(activeOb());
			givenFinalQuestion(round, question(1L, round, "질문 1", 5, false));
			when(answerRepository.existsByRoundIdAndMemberId(ROUND_ID, USER_ID)).thenReturn(false);
			when(questionVoteRepository.findQuestionIdByRoundIdAndMemberId(ROUND_ID, USER_ID)).thenReturn(Optional.empty());

			SopmodamCurrentCardResponse response = sopmodamService.getCurrentCard(USER_ID).orElseThrow();

			assertThat(response.cardType()).isEqualTo(SopmodamCardType.ANSWER_COLLECTING);
			assertThat(response.selectedQuestion()).isEqualTo(new SopmodamQuestionResponse(1L, "질문 1"));
			assertThat(response.canAnswer()).isTrue();
			assertThat(response.hasAnswered()).isFalse();
			assertThat(response.hasVoted()).isFalse();
			assertThat(response.answerCount()).isNull();
		}

		@Test
		@DisplayName("답변 모집 중 명예기수가 이미 답변했으면 답변할 수 없다")
		void 답변_모집_중_명예기수_답변_후() {
			SopmodamRound round = answeringRound();
			givenCurrentRound(round);
			givenUser(honorary());
			givenFinalQuestion(round, question(1L, round, "질문 1", 5, false));
			when(answerRepository.existsByRoundIdAndMemberId(ROUND_ID, USER_ID)).thenReturn(true);

			SopmodamCurrentCardResponse response = sopmodamService.getCurrentCard(USER_ID).orElseThrow();

			assertThat(response.cardType()).isEqualTo(SopmodamCardType.ANSWER_COLLECTING);
			assertThat(response.canAnswer()).isFalse();
			assertThat(response.hasAnswered()).isTrue();
		}

		@Test
		@DisplayName("답변 공개 단계는 답변 수를 내려주고 dDay 는 null 이다")
		void 답변_공개_단계() {
			SopmodamRound round = revealedRound();
			givenCurrentRound(round);
			givenUser(activeOb());
			givenFinalQuestion(round, question(1L, round, "질문 1", 5, false));
			when(answerRepository.countByRoundId(ROUND_ID)).thenReturn(12L);
			when(questionVoteRepository.findQuestionIdByRoundIdAndMemberId(ROUND_ID, USER_ID)).thenReturn(Optional.of(1L));
			when(answerRepository.existsByRoundIdAndMemberId(ROUND_ID, USER_ID)).thenReturn(true);

			SopmodamCurrentCardResponse response = sopmodamService.getCurrentCard(USER_ID).orElseThrow();

			assertThat(response.phase()).isEqualTo(SopmodamPhase.REVEALED);
			assertThat(response.cardType()).isEqualTo(SopmodamCardType.REVEALED);
			assertThat(response.dDay()).isNull();
			assertThat(response.selectedQuestion()).isEqualTo(new SopmodamQuestionResponse(1L, "질문 1"));
			assertThat(response.canVote()).isFalse();
			assertThat(response.canAnswer()).isFalse();
			assertThat(response.hasAnswered()).isTrue();
			assertThat(response.hasVoted()).isTrue();
			assertThat(response.answerCount()).isEqualTo(12L);
			verify(answerRepository).existsByRoundIdAndMemberId(ROUND_ID, USER_ID);
		}

		@Test
		@DisplayName("회차에 저장된 질문 ID로 조회하고 전체 질문 목록은 조회하지 않는다")
		void 지정된_질문이_최종_질문() {
			SopmodamRound round = answeringRound();
			givenCurrentRound(round);
			givenUser(activeYb());
			givenFinalQuestion(round, question(2L, round, "질문 2", 0, false));

			SopmodamCurrentCardResponse response = sopmodamService.getCurrentCard(USER_ID).orElseThrow();

			assertThat(response.selectedQuestion()).isEqualTo(new SopmodamQuestionResponse(2L, "질문 2"));
			verify(questionRepository, never()).findAllByRoundIdOrderByIdAsc(anyLong());
		}

		@Test
		@DisplayName("답변 모집 중 최다 득표가 동률이면 저장하지 않고 카드가 없다")
		void 답변_모집_중_동률이면_카드_없음() {
			SopmodamRound round = answeringRound();
			givenCurrentRound(round);
			givenUser(activeOb());
			when(questionRepository.findAllByRoundIdOrderByIdAsc(ROUND_ID)).thenReturn(List.of(
				question(1L, round, "질문 1", 5, false), question(2L, round, "질문 2", 5, false)));
			when(roundRepository.findById(ROUND_ID)).thenReturn(Optional.of(round));

			assertThat(sopmodamService.getCurrentCard(USER_ID)).isEmpty();
			verifyNoInteractions(answerRepository);
			verify(roundRepository, never()).selectQuestionIfAbsent(anyLong(), anyLong());
			verify(questionRepository, never()).updateSelectedQuestionInRound(anyLong(), anyLong());
		}

		@Test
		@DisplayName("단독 최다 득표를 저장하고 다시 읽은 질문으로 카드를 반환한다")
		void 단독_최다_득표_자동_선정() {
			SopmodamRound round = answeringRound();
			SopmodamRound stored = answeringRound();
			ReflectionTestUtils.setField(stored, "selectedQuestionId", 2L);
			givenCurrentRound(round);
			givenUser(activeYb());
			SopmodamQuestion winner = question(2L, round, "질문 2", 5, false);
			when(questionRepository.findAllByRoundIdOrderByIdAsc(ROUND_ID))
				.thenReturn(List.of(question(1L, round, "질문 1", 3, false), winner));
			when(roundRepository.selectQuestionIfAbsent(ROUND_ID, 2L)).thenReturn(1);
			when(roundRepository.findById(ROUND_ID)).thenReturn(Optional.of(stored));
			when(questionRepository.findByIdAndRoundId(2L, ROUND_ID)).thenReturn(Optional.of(winner));

			SopmodamCurrentCardResponse response = sopmodamService.getCurrentCard(USER_ID).orElseThrow();

			assertThat(response.selectedQuestion()).isEqualTo(new SopmodamQuestionResponse(2L, "질문 2"));
			verify(roundRepository).selectQuestionIfAbsent(ROUND_ID, 2L);
			verify(questionRepository).updateSelectedQuestionInRound(ROUND_ID, 2L);
			verify(roundRepository).findById(ROUND_ID);
		}

		@Test
		@DisplayName("다른 요청이 먼저 저장했다면 계산한 후보 대신 실제 저장된 질문을 사용한다")
		void 다른_요청이_선정한_질문_유지() {
			SopmodamRound round = answeringRound();
			SopmodamRound stored = answeringRound();
			ReflectionTestUtils.setField(stored, "selectedQuestionId", 1L);
			givenCurrentRound(round);
			givenUser(activeYb());
			SopmodamQuestion selected = question(1L, round, "질문 1", 3, false);
			when(questionRepository.findAllByRoundIdOrderByIdAsc(ROUND_ID))
				.thenReturn(List.of(selected, question(2L, round, "질문 2", 5, false)));
			when(roundRepository.selectQuestionIfAbsent(ROUND_ID, 2L)).thenReturn(0);
			when(roundRepository.findById(ROUND_ID)).thenReturn(Optional.of(stored));
			when(questionRepository.findByIdAndRoundId(1L, ROUND_ID)).thenReturn(Optional.of(selected));

			SopmodamCurrentCardResponse response = sopmodamService.getCurrentCard(USER_ID).orElseThrow();

			assertThat(response.selectedQuestion()).isEqualTo(new SopmodamQuestionResponse(1L, "질문 1"));
			verify(questionRepository, never()).findByIdAndRoundId(2L, ROUND_ID);
			verify(questionRepository, never()).updateSelectedQuestionInRound(anyLong(), anyLong());
		}

	}

	@Nested
	@DisplayName("U4 답변 작성")
	class CreateAnswer {

		private static final String VALID_CONTENT = "솝트에서 배운 협업 경험을 바탕으로 말씀드리겠습니다.";

		@Test
		@DisplayName("활동 OB 가 답변 모집 기간에 유효한 내용으로 처음 작성하면 저장한다")
		void 정상_답변_저장() {
			SopmodamRound round = answeringRound();
			SopmodamQuestion selected = selectedQuestion(10L, round);
			Member member = Member.builder().id(USER_ID).build();
			SopmodamAnswer answer = mock(SopmodamAnswer.class);
			when(answer.getId()).thenReturn(10L);
			when(answer.getContent()).thenReturn(VALID_CONTENT);
			when(answer.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 10, 20, 13, 0));
			when(answer.getUpdatedAt()).thenReturn(LocalDateTime.of(2026, 10, 20, 13, 0));

			givenRound(round);
			givenUser(activeOb());
			when(questionRepository.findByRoundIdAndIsSelectedTrue(ROUND_ID)).thenReturn(Optional.of(selected));
			when(answerRepository.existsByQuestionIdAndMemberId(selected.getId(), USER_ID)).thenReturn(false);
			when(memberRetriever.findMemberById(USER_ID)).thenReturn(member);
			when(answerModifier.createAnswer(selected, member, VALID_CONTENT)).thenReturn(answer);

			SopmodamAnswerCreateResponse response = sopmodamService.createAnswer(USER_ID, ROUND_ID, new SopmodamAnswerCreateRequest(VALID_CONTENT));

			assertThat(response.answerId()).isEqualTo(10L);
			assertThat(response.content()).isEqualTo(VALID_CONTENT);
			verify(answerModifier).createAnswer(selected, member, VALID_CONTENT);
		}

		@Test
		@DisplayName("명예기수도 답변할 수 있다")
		void 명예기수도_답변_가능() {
			SopmodamRound round = answeringRound();
			SopmodamQuestion selected = selectedQuestion(10L, round);
			Member member = Member.builder().id(USER_ID).build();
			SopmodamAnswer answer = mock(SopmodamAnswer.class);
			when(answer.getId()).thenReturn(10L);
			when(answer.getContent()).thenReturn(VALID_CONTENT);
			when(answer.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 10, 20, 13, 0));
			when(answer.getUpdatedAt()).thenReturn(LocalDateTime.of(2026, 10, 20, 13, 0));

			givenRound(round);
			givenUser(honorary());
			when(questionRepository.findByRoundIdAndIsSelectedTrue(ROUND_ID)).thenReturn(Optional.of(selected));
			when(answerRepository.existsByQuestionIdAndMemberId(selected.getId(), USER_ID)).thenReturn(false);
			when(memberRetriever.findMemberById(USER_ID)).thenReturn(member);
			when(answerModifier.createAnswer(selected, member, VALID_CONTENT)).thenReturn(answer);

			assertThat(sopmodamService.createAnswer(USER_ID, ROUND_ID, new SopmodamAnswerCreateRequest(VALID_CONTENT)).answerId()).isEqualTo(10L);
		}

		// ── 내용 검증 ──────────────────────────────────────────────────────────

		@Test
		@DisplayName("content 가 null 이면 400 이고 DB 를 조회하지 않는다")
		void content가_null이면_400() {
			assertThatThrownBy(() -> sopmodamService.createAnswer(USER_ID, ROUND_ID, new SopmodamAnswerCreateRequest(null)))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining("답변 글자수가 초과되었습니다.");
			verifyNoInteractions(roundRepository, platformService, answerModifier);
		}

		@Test
		@DisplayName("content 가 501자 이상이면 400")
		void content가_500자_초과면_400() {
			String over500 = "가".repeat(501);

			assertThatThrownBy(() -> sopmodamService.createAnswer(USER_ID, ROUND_ID, new SopmodamAnswerCreateRequest(over500)))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining("답변 글자수가 초과되었습니다.");
			verifyNoInteractions(roundRepository, platformService, answerModifier);
		}

		@Test
		@DisplayName("공백으로만 이루어진 content 는 400")
		void 공백만_이루어진_content는_400() {
			String allSpaces = " ".repeat(100);

			assertThatThrownBy(() -> sopmodamService.createAnswer(USER_ID, ROUND_ID, new SopmodamAnswerCreateRequest(allSpaces)))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining("답변 내용을 입력해주세요.");
			verifyNoInteractions(roundRepository, platformService, answerModifier);
		}

		@Test
		@DisplayName("탭·줄바꿈으로만 이루어진 content 도 400")
		void 탭_줄바꿈만_이루어진_content는_400() {
			String tabsAndNewlines = "\t\n\r";

			assertThatThrownBy(() -> sopmodamService.createAnswer(USER_ID, ROUND_ID, new SopmodamAnswerCreateRequest(tabsAndNewlines)))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining("답변 내용을 입력해주세요.");
			verifyNoInteractions(roundRepository, platformService, answerModifier);
		}

		@Test
		@DisplayName("짧은 내용(1자)도 유효하다")
		void 짧은_내용도_유효하다() {
			String oneChar = "가";
			SopmodamRound round = answeringRound();
			SopmodamQuestion selected = selectedQuestion(10L, round);
			Member member = Member.builder().id(USER_ID).build();
			SopmodamAnswer answer = mock(SopmodamAnswer.class);
			when(answer.getId()).thenReturn(10L);
			when(answer.getContent()).thenReturn(oneChar);
			when(answer.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 10, 20, 13, 0));
			when(answer.getUpdatedAt()).thenReturn(LocalDateTime.of(2026, 10, 20, 13, 0));

			givenRound(round);
			givenUser(activeOb());
			when(questionRepository.findByRoundIdAndIsSelectedTrue(ROUND_ID)).thenReturn(Optional.of(selected));
			when(answerRepository.existsByQuestionIdAndMemberId(selected.getId(), USER_ID)).thenReturn(false);
			when(memberRetriever.findMemberById(USER_ID)).thenReturn(member);
			when(answerModifier.createAnswer(selected, member, oneChar)).thenReturn(answer);

			assertThat(sopmodamService.createAnswer(USER_ID, ROUND_ID, new SopmodamAnswerCreateRequest(oneChar)).answerId()).isEqualTo(10L);
		}

		// ── 회차 검증 ──────────────────────────────────────────────────────────

		@Test
		@DisplayName("내용 검증 통과 후 회차가 없으면 404 이고 Platform 을 호출하지 않는다")
		void 회차가_없으면_404() {
			when(roundRepository.findById(ROUND_ID)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> sopmodamService.createAnswer(USER_ID, ROUND_ID, new SopmodamAnswerCreateRequest(VALID_CONTENT)))
				.isInstanceOf(NotFoundException.class)
				.hasMessageContaining("존재하지 않는 솝모담 회차입니다.");
			verifyNoInteractions(platformService, questionRepository, answerModifier);
		}

		// ── 권한 검증 ──────────────────────────────────────────────────────────

		@Test
		@DisplayName("활동 YB 는 403 이고 phase 검사로 넘어가지 않는다")
		void 활동_YB는_403() {
			givenRound(answeringRound());
			givenUser(activeYb());

			assertThatThrownBy(() -> sopmodamService.createAnswer(USER_ID, ROUND_ID, new SopmodamAnswerCreateRequest(VALID_CONTENT)))
				.isInstanceOf(ForbiddenException.class)
				.hasMessageContaining("답변 권한이 없습니다.");
			verifyNoInteractions(questionRepository, answerModifier);
		}

		@Test
		@DisplayName("회차 기수보다 뒤 기수인 사용자도 403")
		void 뒤_기수_사용자는_403() {
			givenRound(answeringRound());
			givenUser(laterGeneration());

			assertThatThrownBy(() -> sopmodamService.createAnswer(USER_ID, ROUND_ID, new SopmodamAnswerCreateRequest(VALID_CONTENT)))
				.isInstanceOf(ForbiddenException.class)
				.hasMessageContaining("답변 권한이 없습니다.");
			verifyNoInteractions(answerModifier);
		}

		@Test
		@DisplayName("권한이 없는 사용자가 투표 기간 회차에 요청해도 권한(403)이 기간(400)보다 먼저다")
		void 활동_YB_투표기간_회차는_403이_먼저() {
			givenRound(votingRound());
			givenUser(activeYb());

			assertThatThrownBy(() -> sopmodamService.createAnswer(USER_ID, ROUND_ID, new SopmodamAnswerCreateRequest(VALID_CONTENT)))
				.isInstanceOf(ForbiddenException.class);
		}

		// ── Phase 검증 ─────────────────────────────────────────────────────────

		@Test
		@DisplayName("투표 기간(VOTING)에는 400")
		void 투표_기간이면_400() {
			givenRound(votingRound());
			givenUser(activeOb());

			assertThatThrownBy(() -> sopmodamService.createAnswer(USER_ID, ROUND_ID, new SopmodamAnswerCreateRequest(VALID_CONTENT)))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining("답변 모집 기간이 아닙니다.");
			verifyNoInteractions(questionRepository, answerModifier);
		}

		@Test
		@DisplayName("투표 시작 전(SCHEDULED)이면 400")
		void 투표_시작전이면_400() {
			givenRound(scheduledRound());
			givenUser(activeOb());

			assertThatThrownBy(() -> sopmodamService.createAnswer(USER_ID, ROUND_ID, new SopmodamAnswerCreateRequest(VALID_CONTENT)))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining("답변 모집 기간이 아닙니다.");
			verifyNoInteractions(questionRepository, answerModifier);
		}

		@Test
		@DisplayName("공개 단계(REVEALED)면 400")
		void 공개_단계면_400() {
			givenRound(revealedRound());
			givenUser(activeOb());

			assertThatThrownBy(() -> sopmodamService.createAnswer(USER_ID, ROUND_ID, new SopmodamAnswerCreateRequest(VALID_CONTENT)))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining("답변 모집 기간이 아닙니다.");
			verifyNoInteractions(answerModifier);
		}

		// ── 선정 질문 검증 ─────────────────────────────────────────────────────

		@Test
		@DisplayName("선정된 질문이 없으면 400 이고 회원 조회·저장으로 넘어가지 않는다")
		void 선정_질문_없으면_400() {
			givenRound(answeringRound());
			givenUser(activeOb());
			when(questionRepository.findByRoundIdAndIsSelectedTrue(ROUND_ID)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> sopmodamService.createAnswer(USER_ID, ROUND_ID, new SopmodamAnswerCreateRequest(VALID_CONTENT)))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining("최종 질문이 선정되지 않았습니다.");
			verifyNoInteractions(memberRetriever, answerModifier);
		}

		// ── 회원·중복 검증 ─────────────────────────────────────────────────────

		@Test
		@DisplayName("이미 답변했으면 409 이고 회원 조회·저장으로 넘어가지 않는다")
		void 이미_답변했으면_409() {
			SopmodamRound round = answeringRound();
			SopmodamQuestion selected = selectedQuestion(10L, round);
			givenRound(round);
			givenUser(activeOb());
			when(questionRepository.findByRoundIdAndIsSelectedTrue(ROUND_ID)).thenReturn(Optional.of(selected));
			when(answerRepository.existsByQuestionIdAndMemberId(selected.getId(), USER_ID)).thenReturn(true);

			assertThatThrownBy(() -> sopmodamService.createAnswer(USER_ID, ROUND_ID, new SopmodamAnswerCreateRequest(VALID_CONTENT)))
				.isInstanceOf(ConflictException.class)
				.hasMessageContaining("이미 답변하셨습니다.");
			verify(memberRetriever, never()).findMemberById(anyLong());
			verify(answerModifier, never()).createAnswer(any(), any(), any());
		}

		@Test
		@DisplayName("플레이그라운드 회원이 없으면 저장하지 않는다")
		void 회원이_없으면_저장하지_않는다() {
			SopmodamRound round = answeringRound();
			SopmodamQuestion selected = selectedQuestion(10L, round);
			givenRound(round);
			givenUser(activeOb());
			when(questionRepository.findByRoundIdAndIsSelectedTrue(ROUND_ID)).thenReturn(Optional.of(selected));
			when(answerRepository.existsByQuestionIdAndMemberId(selected.getId(), USER_ID)).thenReturn(false);
			when(memberRetriever.findMemberById(USER_ID)).thenThrow(new NotFoundException("존재하지 않는 사용자의 id값 입니다."));

			assertThatThrownBy(() -> sopmodamService.createAnswer(USER_ID, ROUND_ID, new SopmodamAnswerCreateRequest(VALID_CONTENT)))
				.isInstanceOf(NotFoundException.class);
			verify(answerModifier, never()).createAnswer(any(), any(), any());
		}
	}

	@Nested
	@DisplayName("U5 답변 수정")
	class UpdateAnswer {

		private static final Long ANSWER_ID = 20L;
		private static final String VALID_CONTENT = "솝트에서 배운 협업 경험을 바탕으로 말씀드리겠습니다.";
		private static final String NEW_CONTENT = "수정된 답변입니다. 공백 제외 이십 자 이상이어야 합니다요.";

		private SopmodamAnswer answerOf(SopmodamRound round) {
			SopmodamQuestion question = selectedQuestion(10L, round);
			Member member = Member.builder().id(USER_ID).build();
			return SopmodamAnswer.builder().id(ANSWER_ID).question(question).member(member).content(VALID_CONTENT).build();
		}

		private void givenAnswer(SopmodamAnswer answer) {
			when(answerRepository.findByIdWithDetails(ANSWER_ID)).thenReturn(Optional.of(answer));
		}

		@Test
		@DisplayName("본인 답변을 답변 모집 기간에 유효한 내용으로 수정하면 저장한다")
		void 정상_수정() {
			SopmodamRound round = answeringRound();
			SopmodamAnswer answer = answerOf(round);
			SopmodamAnswer updated = mock(SopmodamAnswer.class);
			when(updated.getId()).thenReturn(ANSWER_ID);
			when(updated.getContent()).thenReturn(NEW_CONTENT);
			when(updated.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 10, 20, 13, 0));
			when(updated.getUpdatedAt()).thenReturn(LocalDateTime.of(2026, 10, 21, 9, 0));

			givenAnswer(answer);
			when(answerModifier.updateAnswer(answer, NEW_CONTENT)).thenReturn(updated);

			SopmodamAnswerUpdateResponse response = sopmodamService.updateAnswer(USER_ID, ANSWER_ID, new SopmodamAnswerUpdateRequest(NEW_CONTENT));

			assertThat(response.answerId()).isEqualTo(ANSWER_ID);
			assertThat(response.content()).isEqualTo(NEW_CONTENT);
			verify(answerModifier).updateAnswer(answer, NEW_CONTENT);
		}

		// ── 내용 검증 ──────────────────────────────────────────────────────────

		@Test
		@DisplayName("content 가 null 이면 답변을 조회하지 않고 400")
		void content가_null이면_400() {
			assertThatThrownBy(() -> sopmodamService.updateAnswer(USER_ID, ANSWER_ID, new SopmodamAnswerUpdateRequest(null)))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining("답변 글자수가 초과되었습니다.");
			verifyNoInteractions(answerRepository, answerModifier);
		}

		@Test
		@DisplayName("content 가 501자 이상이면 400")
		void content가_500자_초과면_400() {
			String over500 = "가".repeat(501);

			assertThatThrownBy(() -> sopmodamService.updateAnswer(USER_ID, ANSWER_ID, new SopmodamAnswerUpdateRequest(over500)))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining("답변 글자수가 초과되었습니다.");
			verifyNoInteractions(answerRepository, answerModifier);
		}

		@Test
		@DisplayName("공백으로만 이루어진 content 는 400")
		void 공백만_이루어진_content는_400() {
			String allSpaces = " ".repeat(100);

			assertThatThrownBy(() -> sopmodamService.updateAnswer(USER_ID, ANSWER_ID, new SopmodamAnswerUpdateRequest(allSpaces)))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining("답변 내용을 입력해주세요.");
			verifyNoInteractions(answerRepository, answerModifier);
		}

		@Test
		@DisplayName("탭·줄바꿈으로만 이루어진 content 도 400")
		void 탭_줄바꿈만_이루어진_content는_400() {
			String tabsAndNewlines = "\t\n\r";

			assertThatThrownBy(() -> sopmodamService.updateAnswer(USER_ID, ANSWER_ID, new SopmodamAnswerUpdateRequest(tabsAndNewlines)))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining("답변 내용을 입력해주세요.");
			verifyNoInteractions(answerRepository, answerModifier);
		}

		// ── 답변 조회 검증 ─────────────────────────────────────────────────────

		@Test
		@DisplayName("답변이 없으면 404 이고 수정하지 않는다")
		void 답변이_없으면_404() {
			when(answerRepository.findByIdWithDetails(ANSWER_ID)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> sopmodamService.updateAnswer(USER_ID, ANSWER_ID, new SopmodamAnswerUpdateRequest(VALID_CONTENT)))
				.isInstanceOf(NotFoundException.class)
				.hasMessageContaining("존재하지 않는 답변입니다.");
			verifyNoInteractions(answerModifier);
		}

		// ── 소유권 검증 ────────────────────────────────────────────────────────

		@Test
		@DisplayName("본인의 답변이 아니면 403 이고 phase 검사로 넘어가지 않는다")
		void 본인_답변_아니면_403() {
			SopmodamRound round = answeringRound();
			SopmodamQuestion question = selectedQuestion(10L, round);
			Member otherMember = Member.builder().id(999L).build();
			SopmodamAnswer otherAnswer = SopmodamAnswer.builder()
				.id(ANSWER_ID).question(question).member(otherMember).content(VALID_CONTENT).build();
			givenAnswer(otherAnswer);

			assertThatThrownBy(() -> sopmodamService.updateAnswer(USER_ID, ANSWER_ID, new SopmodamAnswerUpdateRequest(VALID_CONTENT)))
				.isInstanceOf(ForbiddenException.class)
				.hasMessageContaining("본인의 답변이 아닙니다.");
			verifyNoInteractions(answerModifier);
		}

		@Test
		@DisplayName("본인 답변이 아닌 경우 phase 가 ANSWERING 이 아니어도 403 이 먼저다")
		void 비소유자_투표기간_회차면_403이_먼저() {
			SopmodamRound round = votingRound();
			SopmodamQuestion question = selectedQuestion(10L, round);
			Member otherMember = Member.builder().id(999L).build();
			SopmodamAnswer otherAnswer = SopmodamAnswer.builder()
				.id(ANSWER_ID).question(question).member(otherMember).content(VALID_CONTENT).build();
			givenAnswer(otherAnswer);

			assertThatThrownBy(() -> sopmodamService.updateAnswer(USER_ID, ANSWER_ID, new SopmodamAnswerUpdateRequest(VALID_CONTENT)))
				.isInstanceOf(ForbiddenException.class);
			verifyNoInteractions(answerModifier);
		}

		// ── Phase 검증 ─────────────────────────────────────────────────────────

		@Test
		@DisplayName("투표 기간(VOTING)이면 400")
		void 투표_기간이면_400() {
			givenAnswer(answerOf(votingRound()));

			assertThatThrownBy(() -> sopmodamService.updateAnswer(USER_ID, ANSWER_ID, new SopmodamAnswerUpdateRequest(VALID_CONTENT)))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining("답변 모집 기간이 아닙니다.");
			verifyNoInteractions(answerModifier);
		}

		@Test
		@DisplayName("투표 시작 전(SCHEDULED)이면 400")
		void 투표_시작전이면_400() {
			givenAnswer(answerOf(scheduledRound()));

			assertThatThrownBy(() -> sopmodamService.updateAnswer(USER_ID, ANSWER_ID, new SopmodamAnswerUpdateRequest(VALID_CONTENT)))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining("답변 모집 기간이 아닙니다.");
			verifyNoInteractions(answerModifier);
		}

		@Test
		@DisplayName("공개 단계(REVEALED)면 400")
		void 공개_단계면_400() {
			givenAnswer(answerOf(revealedRound()));

			assertThatThrownBy(() -> sopmodamService.updateAnswer(USER_ID, ANSWER_ID, new SopmodamAnswerUpdateRequest(VALID_CONTENT)))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining("답변 모집 기간이 아닙니다.");
			verifyNoInteractions(answerModifier);
		}
	}

	private void givenRound(SopmodamRound round) {
		when(roundRepository.findById(ROUND_ID)).thenReturn(Optional.of(round));
	}

	private void givenCurrentRound(SopmodamRound round) {
		when(roundRepository.findFirstByVoteStartAtLessThanEqualOrderByVoteStartAtDesc(any(LocalDateTime.class)))
			.thenReturn(Optional.of(round));
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

	private void givenFinalQuestion(SopmodamRound round, SopmodamQuestion question) {
		ReflectionTestUtils.setField(round, "selectedQuestionId", question.getId());
		when(questionRepository.findByIdAndRoundId(question.getId(), round.getId()))
			.thenReturn(Optional.of(question));
	}

	private SopmodamRound scheduledRound() {
		return round(today.plusDays(1), today.plusDays(6), today.plusDays(20));
	}

	// 투표 마감이 5일 뒤 0시 → 오늘 기준 D-4
	private SopmodamRound votingRound() {
		return round(today.minusDays(1), today.plusDays(5), today.plusDays(20));
	}

	// 답변 마감이 10일 뒤 0시 → 오늘 기준 D-9
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

	private SopmodamQuestion question(Long questionId, SopmodamRound round, String content, int voteCount, boolean isSelected) {
		return SopmodamQuestion.builder()
			.id(questionId)
			.round(round)
			.content(content)
			.voteCount(voteCount)
			.isSelected(isSelected)
			.build();
	}

	private SopmodamQuestion selectedQuestion(Long questionId, SopmodamRound round) {
		return SopmodamQuestion.builder().id(questionId).round(round).content("선정된 질문").isSelected(true).build();
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
