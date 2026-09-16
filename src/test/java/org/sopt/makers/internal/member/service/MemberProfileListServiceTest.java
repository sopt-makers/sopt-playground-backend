package org.sopt.makers.internal.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.sopt.makers.internal.coffeechat.service.CoffeeChatRetriever;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.external.platform.PlatformService;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.dto.profile.MemberActivityCountVo;
import org.sopt.makers.internal.member.dto.profile.MemberBasicInfoVo;
import org.sopt.makers.internal.member.dto.profile.MemberFavorVo;
import org.sopt.makers.internal.member.dto.profile.MemberIntroVo;
import org.sopt.makers.internal.member.dto.profile.MemberPersonalityVo;
import org.sopt.makers.internal.member.dto.profile.MemberProfileSummaryVo;
import org.sopt.makers.internal.member.dto.response.MemberAllProfileResponse;
import org.sopt.makers.internal.member.dto.response.MemberProfileResponse;
import org.sopt.makers.internal.member.mapper.MemberMapper;
import org.sopt.makers.internal.member.mapper.MemberResponseMapper;
import org.sopt.makers.internal.member.repository.MemberProfileQueryRepository;
import org.sopt.makers.internal.member.repository.MemberRepository;
import org.sopt.makers.internal.member.service.sorting.MemberSortingService;

/**
 * 프로필 목록 조회의 오케스트레이션 테스트.
 *
 * <p>정렬·가중치 자체는 별도 단위 테스트에서 다룬다. 여기서는 흐름만 본다.
 * <ul>
 *   <li>단계별 조기 리턴이 불필요한 하위 호출을 막는가</li>
 *   <li>페이징 계산(hasNext / totalCount)이 맞는가</li>
 *   <li><b>엔티티를 페이지 대상에 대해서만 fetch join 으로 로드하는가</b></li>
 * </ul>
 *
 * <p>트랜잭션 경계 자체(플랫폼 호출이 트랜잭션 밖인지)는 프록시가 없는 단위 테스트로 검증할 수 없다.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MemberProfileListServiceTest {

	@Mock MemberRepository memberRepository;
	@Mock MemberProfileQueryRepository memberProfileQueryRepository;
	@Mock MemberQuestionRetriever memberQuestionRetriever;
	@Mock CoffeeChatRetriever coffeeChatRetriever;
	@Mock PlatformService platformService;
	@Mock MemberSortingService memberSortingService;
	@Mock MemberMapper memberMapper;
	@Mock MemberResponseMapper memberResponseMapper;

	private MemberProfileListService memberProfileListService;

	/**
	 * DB 접근 구간을 담당하는 {@link MemberProfileListRetriever} 는 mock 이 아니라 실제 인스턴스를 쓴다.
	 * 리트리버가 트랜잭션 경계일 뿐 조회 로직 자체는 얇은 위임이라, mock 으로 덮으면
	 * "페이지 대상만 fetch join 으로 로드한다" 같은 검증이 통째로 사라진다.
	 * (트랜잭션은 프록시가 없는 단위 테스트에서 어차피 동작하지 않는다.)
	 */
	@BeforeEach
	void setUp() {
		MemberProfileListRetriever memberProfileListRetriever = new MemberProfileListRetriever(
			memberRepository,
			memberProfileQueryRepository,
			memberQuestionRetriever,
			coffeeChatRetriever
		);
		memberProfileListService = new MemberProfileListService(
			memberProfileListRetriever,
			platformService,
			memberSortingService,
			memberMapper,
			memberResponseMapper
		);
	}

	@Test
	@DisplayName("DB 필터 결과가 없으면 플랫폼을 호출하지 않고 빈 응답을 반환한다")
	void DB_필터_결과가_없으면_조기_리턴() {
		when(memberProfileQueryRepository.findAllMemberIdsByDbFilters(any(), any(), any()))
			.thenReturn(List.of());

		MemberAllProfileResponse response = getProfiles(null, null, null);

		assertThat(response.members()).isEmpty();
		assertThat(response.hasNext()).isFalse();
		assertThat(response.totalMembersCount()).isZero();
		verify(platformService, never()).getInternalUsers(anyList());
	}

	@Test
	@DisplayName("플랫폼 필터 후 남는 멤버가 없으면 Projection 을 조회하지 않는다")
	void 플랫폼_필터_후_0명이면_조기_리턴() {
		when(memberProfileQueryRepository.findAllMemberIdsByDbFilters(any(), any(), any()))
			.thenReturn(List.of(1L));
		when(platformService.getInternalUsers(anyList())).thenReturn(List.of());

		MemberAllProfileResponse response = getProfiles(null, null, null);

		assertThat(response.members()).isEmpty();
		verify(memberProfileQueryRepository, never()).findMemberProfileSummariesByIds(anyList());
	}

	@Test
	@DisplayName("검색 결과가 없으면 엔티티를 로드하지 않는다")
	void 검색_결과가_없으면_조기_리턴() {
		givenMembers(3);

		MemberAllProfileResponse response = getProfiles(null, null, "없는검색어");

		assertThat(response.members()).isEmpty();
		verify(memberRepository, never()).findAllByIdInWithCareers(anyList());
		verify(memberRepository, never()).findAllByIdInWithLinks(anyList());
	}

	@Test
	@DisplayName("첫 페이지는 hasNext 가 true 이고 totalCount 는 전체 인원이다")
	void 첫_페이지_페이징_계산() {
		givenMembers(100);

		MemberAllProfileResponse response = getProfiles(30, 0, null);

		assertThat(response.members()).hasSize(30);
		assertThat(response.hasNext()).isTrue();
		assertThat(response.totalMembersCount()).isEqualTo(100);
	}

	@Test
	@DisplayName("마지막 페이지는 hasNext 가 false 이고 남은 인원만 반환한다")
	void 마지막_페이지_페이징_계산() {
		givenMembers(100);

		MemberAllProfileResponse response = getProfiles(30, 90, null);

		assertThat(response.members()).hasSize(10);
		assertThat(response.hasNext()).isFalse();
		assertThat(response.totalMembersCount()).isEqualTo(100);
	}

	@Test
	@DisplayName("limit 이 없으면 기본값 30을 적용한다")
	void limit_기본값() {
		givenMembers(100);

		MemberAllProfileResponse response = getProfiles(null, null, null);

		assertThat(response.members()).hasSize(30);
	}

	@Test
	@DisplayName("엔티티는 페이지 대상에 대해서만, fetch join 두 메서드로 로드한다")
	void 페이지_대상만_fetch_join으로_로드한다() {
		givenMembers(100);

		getProfiles(30, 0, null);

		// 전체 100명이 아니라 페이지 30건만 넘어가야 한다.
		// 두 메서드를 모두 호출해야 links 와 careers 가 함께 초기화된다.
		// (한쪽이라도 빠지거나 findAllByIdIn 으로 바뀌면 조용히 N+1 이 되살아난다)
		verify(memberRepository).findAllByIdInWithCareers(argThat(ids -> ids.size() == 30));
		verify(memberRepository).findAllByIdInWithLinks(argThat(ids -> ids.size() == 30));
		verify(memberRepository, never()).findAllByIdIn(anyList());
	}

	@Test
	@DisplayName("커피챗 활성 여부는 페이지 인원마다 묻지 않고 한 번에 조회한다")
	void 커피챗_여부는_벌크로_조회한다() {
		givenMembers(100);

		getProfiles(30, 0, null);

		// 멤버마다 존재 여부를 물으면 페이지 크기만큼 쿼리가 나간다
		verify(coffeeChatRetriever, never()).existsCoffeeChat(any());
		verify(coffeeChatRetriever).findActivatedMemberIds(argThat(ids -> ids.size() == 30));
	}

	@Test
	@DisplayName("Projection 조회는 플랫폼 필터를 통과한 전원에 대해 한 번만 호출한다")
	void projection은_전원에_대해_한_번() {
		givenMembers(100);

		getProfiles(30, 0, null);

		verify(memberProfileQueryRepository).findMemberProfileSummariesByIds(argThat(ids -> ids.size() == 100));
	}

	// --- 헬퍼 -------------------------------------------------------------

	private MemberAllProfileResponse getProfiles(Integer limit, Integer offset, String search) {
		return memberProfileListService.getMemberProfiles(
			null, limit, offset, search, null, null, null, null, null);
	}

	/** 필터를 통과한 멤버 count 명이 있는 상태를 만든다. */
	private void givenMembers(int count) {
		List<Long> ids = IntStream.rangeClosed(1, count).mapToObj(Long::valueOf).toList();
		List<InternalUserDetails> users = ids.stream().map(this::userDetails).toList();
		List<MemberProfileSummaryVo> summaries = ids.stream().map(this::summary).toList();
		List<Member> members = ids.stream().map(id -> Member.builder().id(id).build()).toList();

		when(memberProfileQueryRepository.findAllMemberIdsByDbFilters(any(), any(), any())).thenReturn(ids);
		when(platformService.getInternalUsers(anyList())).thenReturn(users);
		when(memberProfileQueryRepository.findMemberProfileSummariesByIds(anyList())).thenReturn(summaries);
		when(memberSortingService.createComparator(any(), any(), any()))
			.thenReturn(Comparator.comparing(InternalUserDetails::userId));
		when(memberRepository.findAllByIdInWithCareers(anyList())).thenAnswer(invocation -> {
			List<Long> requested = invocation.getArgument(0);
			return members.stream().filter(m -> requested.contains(m.getId())).toList();
		});
		when(memberQuestionRetriever.findLatestRecentQuestionsByReceiverIds(anyList(), any()))
			.thenReturn(List.of());
		when(coffeeChatRetriever.findActivatedMemberIds(anyList())).thenReturn(Set.of());
		when(memberMapper.toProfileResponse(any(), any(), any())).thenReturn(profileResponse());
		when(memberResponseMapper.attachQuestionPreview(any(), any())).thenReturn(profileResponse());
	}

	private InternalUserDetails userDetails(Long id) {
		return new InternalUserDetails(id, "멤버" + id, null, null, null, null, 38, List.of());
	}

	private MemberProfileSummaryVo summary(Long id) {
		return new MemberProfileSummaryVo(
			id,
			new MemberBasicInfoVo(null, "서울대학교", null),
			new MemberIntroVo(null, null, null),
			new MemberPersonalityVo(null, null, null, null, null),
			new MemberFavorVo(null, null, null, null, null, null),
			MemberActivityCountVo.EMPTY
		);
	}

	private MemberProfileResponse profileResponse() {
		return new MemberProfileResponse(
			1L, "멤버", null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, List.of(), List.of(), List.of(), null, false, false
		);
	}
}
