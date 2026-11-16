package org.sopt.makers.internal.member.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.makers.internal.coffeechat.service.CoffeeChatRetriever;
import org.sopt.makers.internal.common.Constant;
import org.sopt.makers.internal.community.repository.post.CommunityPostRepository;
import org.sopt.makers.internal.community.service.ReviewService;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.external.platform.PlatformService;
import org.sopt.makers.internal.external.platform.SoptActivity;
import org.sopt.makers.internal.external.slack.SlackClient;
import org.sopt.makers.internal.external.slack.SlackMessageUtil;
import org.sopt.makers.internal.member.constants.AskMemberId;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.domain.TlMember;
import org.sopt.makers.internal.member.domain.enums.ServiceType;
import org.sopt.makers.internal.member.dto.response.AskMemberResponse;
import org.sopt.makers.internal.member.dto.response.TlMemberResponse;
import org.sopt.makers.internal.member.mapper.MemberMapper;
import org.sopt.makers.internal.member.mapper.MemberResponseMapper;
import org.sopt.makers.internal.member.repository.MemberBlockRepository;
import org.sopt.makers.internal.member.repository.MemberLinkRepository;
import org.sopt.makers.internal.member.repository.MemberProfileQueryRepository;
import org.sopt.makers.internal.member.repository.MemberReportRepository;
import org.sopt.makers.internal.member.repository.MemberRepository;
import org.sopt.makers.internal.member.repository.career.MemberCareerRepository;
import org.sopt.makers.internal.member.service.career.MemberCareerRetriever;
import org.sopt.makers.internal.member.service.sorting.MemberSortingService;

import java.util.Collections;
import java.util.List;

import org.sopt.makers.internal.exception.BadRequestException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRetriever memberRetriever;
    @Mock
    private TlMemberRetriever tlMemberRetriever;
    @Mock
    private CoffeeChatRetriever coffeeChatRetriever;
    @Mock
    private MemberCareerRetriever memberCareerRetriever;
    @Mock
    private MemberResponseMapper memberResponseMapper;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private MemberLinkRepository memberLinkRepository;
    @Mock
    private CommunityPostRepository communityPostRepository;
    @Mock
    private MemberCareerRepository memberCareerRepository;
    @Mock
    private MemberProfileQueryRepository memberProfileQueryRepository;
    @Mock
    private MemberReportRepository memberReportRepository;
    @Mock
    private MemberBlockRepository memberBlockRepository;
    @Mock
    private MemberMapper memberMapper;
    @Mock
    private SlackClient slackClient;
    @Mock
    private SlackMessageUtil slackMessageUtil;
    @Mock
    private ReviewService reviewService;
    @Mock
    private PlatformService platformService;
    @Mock
    private MemberSortingService memberSortingService;
    @Mock
    private AskMemberId askMemberId;

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("현재 기수가 아닌 유저가 조회하면 400 에러가 발생한다.")
    void getAppjamTlMembers_ReturnsEmptyList_WhenUserIsNotCurrentGeneration() {
        // Given
        Long userId = 100L;
        Integer previousGeneration = 36;

        InternalUserDetails userDetails = new InternalUserDetails(
                userId,
                "테스트유저",
                null,
                "2024-01-01",
                "010-1234-5678",
                "test@example.com",
                previousGeneration,
                List.of(new SoptActivity(1, previousGeneration, "서버", null, null, true))
        );

        when(platformService.getInternalUser(userId)).thenReturn(userDetails);

        // When & Then
        assertThatThrownBy(() -> memberService.getAppjamTlMembers(userId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("최신 기수가 아닌 유저입니다.");

        verify(platformService).getInternalUser(userId);
        verify(tlMemberRetriever, never()).findByTlGeneration(anyInt());
    }

    @Test
    @DisplayName("현재 기수 유저가 조회하면 TL 멤버 리스트를 반환한다")
    void getAppjamTlMembers_ReturnsTlMemberList_WhenUserIsCurrentGeneration() {
        // Given
        Long userId = 100L;
        Long tlMemberId1 = 1L;
        Long tlMemberId2 = 2L;
        Integer currentGeneration = Constant.CURRENT_GENERATION;

        InternalUserDetails userDetails = new InternalUserDetails(
                userId,
                "테스트유저",
                null,
                "2024-01-01",
                "010-1234-5678",
                "test@example.com",
                currentGeneration,
                List.of(new SoptActivity(1, currentGeneration, "서버", null, null, true))
        );

        Member member1 = Member.builder()
                .id(tlMemberId1)
                .hasProfile(true)
                .university("서울대학교")
                .introduction("안녕하세요")
                .build();

        Member member2 = Member.builder()
                .id(tlMemberId2)
                .hasProfile(true)
                .university("연세대학교")
                .introduction("반갑습니다")
                .build();

        TlMember tlMember1 = TlMember.builder()
                .id(1L)
                .member(member1)
                .tlGeneration(currentGeneration)
                .serviceType(ServiceType.APP)
                .build();

        TlMember tlMember2 = TlMember.builder()
                .id(2L)
                .member(member2)
                .tlGeneration(currentGeneration)
                .serviceType(ServiceType.WEB)
                .build();

        InternalUserDetails tlUserDetails1 = new InternalUserDetails(
                tlMemberId1,
                "김솝트",
                "https://example.com/profile1.jpg",
                "2024-01-01",
                "010-1111-1111",
                "tl1@example.com",
                currentGeneration,
                List.of(new SoptActivity(1, currentGeneration, "안드로이드", null, null, true))
        );

        InternalUserDetails tlUserDetails2 = new InternalUserDetails(
                tlMemberId2,
                "이솝트",
                "https://example.com/profile2.jpg",
                "2024-01-01",
                "010-2222-2222",
                "tl2@example.com",
                currentGeneration,
                List.of(new SoptActivity(2, currentGeneration, "웹", null, null, true))
        );

        when(platformService.getInternalUser(userId)).thenReturn(userDetails);
        when(tlMemberRetriever.findByTlGeneration(currentGeneration))
                .thenReturn(List.of(tlMember1, tlMember2));
        when(memberRepository.findAllByIdIn(List.of(tlMemberId1, tlMemberId2)))
                .thenReturn(List.of(member1, member2));
        when(platformService.getInternalUsers(List.of(tlMemberId1, tlMemberId2)))
                .thenReturn(List.of(tlUserDetails1, tlUserDetails2));

        // When
        List<TlMemberResponse> result = memberService.getAppjamTlMembers(userId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(tlMemberId1);
        assertThat(result.get(0).name()).isEqualTo("김솝트");
        assertThat(result.get(1).id()).isEqualTo(tlMemberId2);
        assertThat(result.get(1).name()).isEqualTo("이솝트");

        verify(platformService).getInternalUser(userId);
        verify(tlMemberRetriever).findByTlGeneration(currentGeneration);
        verify(memberRepository).findAllByIdIn(anyList());
        verify(platformService).getInternalUsers(anyList());
    }

    @Test
    @DisplayName("TL 멤버가 없으면 빈 리스트를 반환한다")
    void getAppjamTlMembers_ReturnsEmptyList_WhenNoTlMembers() {
        // Given
        Long userId = 100L;
        Integer currentGeneration = Constant.CURRENT_GENERATION;

        InternalUserDetails userDetails = new InternalUserDetails(
                userId,
                "테스트유저",
                null,
                "2024-01-01",
                "010-1234-5678",
                "test@example.com",
                currentGeneration,
                List.of(new SoptActivity(1, currentGeneration, "서버", null, null, true))
        );

        when(platformService.getInternalUser(userId)).thenReturn(userDetails);
        when(tlMemberRetriever.findByTlGeneration(currentGeneration))
                .thenReturn(Collections.emptyList());
        when(memberRepository.findAllByIdIn(Collections.emptyList()))
                .thenReturn(Collections.emptyList());

        // When
        List<TlMemberResponse> result = memberService.getAppjamTlMembers(userId);

        // Then
        assertThat(result).isEmpty();
        verify(platformService).getInternalUser(userId);
        verify(tlMemberRetriever).findByTlGeneration(currentGeneration);
    }

    @Test
    @DisplayName("프로필이 없는 TL 멤버도 결과에 포함된다")
    void getAppjamTlMembers_IncludesMembers_WithoutProfile() {
        // Given
        Long userId = 100L;
        Long tlMemberId1 = 1L;
        Long tlMemberId2 = 2L;
        Integer currentGeneration = Constant.CURRENT_GENERATION;

        InternalUserDetails userDetails = new InternalUserDetails(
                userId,
                "테스트유저",
                null,
                "2024-01-01",
                "010-1234-5678",
                "test@example.com",
                currentGeneration,
                List.of(new SoptActivity(1, currentGeneration, "서버", null, null, true))
        );

        Member member1 = Member.builder()
                .id(tlMemberId1)
                .hasProfile(true)
                .university("서울대학교")
                .introduction("안녕하세요")
                .build();

        TlMember tlMember1 = TlMember.builder()
                .id(1L)
                .member(member1)
                .tlGeneration(currentGeneration)
                .serviceType(ServiceType.APP)
                .build();

        Member member2WithoutProfile = Member.builder()
                .id(tlMemberId2)
                .hasProfile(false)
                .build();

        TlMember tlMember2 = TlMember.builder()
                .id(2L)
                .member(member2WithoutProfile)
                .tlGeneration(currentGeneration)
                .serviceType(ServiceType.WEB)
                .build();

        InternalUserDetails tlUserDetails1 = new InternalUserDetails(
                tlMemberId1,
                "김솝트",
                "https://example.com/profile1.jpg",
                "2024-01-01",
                "010-1111-1111",
                "tl1@example.com",
                currentGeneration,
                List.of(new SoptActivity(1, currentGeneration, "안드로이드", null, null, true))
        );

        InternalUserDetails tlUserDetails2 = new InternalUserDetails(
                tlMemberId2,
                "이솝트",
                "https://example.com/profile2.jpg",
                "2024-01-01",
                "010-2222-2222",
                "tl2@example.com",
                currentGeneration,
                List.of(new SoptActivity(2, currentGeneration, "웹", null, null, true))
        );

        when(platformService.getInternalUser(userId)).thenReturn(userDetails);
        when(tlMemberRetriever.findByTlGeneration(currentGeneration))
                .thenReturn(List.of(tlMember1, tlMember2));
        when(memberRepository.findAllByIdIn(List.of(tlMemberId1, tlMemberId2)))
                .thenReturn(List.of(member1, member2WithoutProfile));
        when(platformService.getInternalUsers(List.of(tlMemberId1, tlMemberId2)))
                .thenReturn(List.of(tlUserDetails1, tlUserDetails2));

        // When
        List<TlMemberResponse> result = memberService.getAppjamTlMembers(userId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(tlMemberId1);
        assertThat(result.get(0).name()).isEqualTo("김솝트");
        assertThat(result.get(1).id()).isEqualTo(tlMemberId2);
        assertThat(result.get(1).name()).isEqualTo("이솝트");

        verify(platformService).getInternalUser(userId);
        verify(tlMemberRetriever).findByTlGeneration(currentGeneration);
    }

    @Test
    @DisplayName("질문 대상 멤버는 DB·플랫폼 정보를 한 번씩 일괄 조회하고 대상 ID 순서대로 응답한다")
    void getAskMembers_FetchesInBatchAndKeepsOrder() {
        // Given
        List<Long> memberIds = List.of(3L, 1L, 2L);
        when(askMemberId.getAllAskMembers(false)).thenReturn(memberIds);
        when(memberRepository.findAllByIdInWithCareers(memberIds))
                .thenReturn(List.of(askMember(1L), askMember(2L), askMember(3L)));
        when(platformService.getInternalUsers(memberIds)).thenReturn(List.of(
                askUserDetails(1L),
                askUserDetails(2L),
                askUserDetails(3L)
        ));

        // When
        AskMemberResponse response = memberService.getAskMembers(null);

        // Then
        assertThat(response.members())
                .extracting(AskMemberResponse.QuestionTargetMember::id)
                .containsExactly(3L, 1L, 2L);
        assertThat(response.members())
                .extracting(AskMemberResponse.QuestionTargetMember::name)
                .containsExactly("멤버3", "멤버1", "멤버2");

        verify(memberRepository, times(1)).findAllByIdInWithCareers(memberIds);
        verify(platformService, times(1)).getInternalUsers(memberIds);
        verify(platformService, never()).getInternalUserDetailsMap(any());
        verify(memberRepository, never()).findById(any());
        verify(platformService, never()).getInternalUser(any());
    }

    @Test
    @DisplayName("DB에 없는 회원은 응답과 플랫폼 조회 대상에서 제외한다")
    void getAskMembers_SkipsMemberNotFoundInDb() {
        // Given
        List<Long> memberIds = List.of(1L, 2L);
        when(askMemberId.getAllAskMembers(false)).thenReturn(memberIds);
        when(memberRepository.findAllByIdInWithCareers(memberIds)).thenReturn(List.of(askMember(2L)));
        when(platformService.getInternalUsers(List.of(2L)))
                .thenReturn(List.of(askUserDetails(2L)));

        // When
        AskMemberResponse response = memberService.getAskMembers(null);

        // Then
        assertThat(response.members())
                .extracting(AskMemberResponse.QuestionTargetMember::id)
                .containsExactly(2L);
        verify(platformService).getInternalUsers(List.of(2L));
    }

    @Test
    @DisplayName("플랫폼 정보가 누락되거나 활동 이력이 없는 회원은 단건 재조회 없이 응답에서 제외한다")
    void getAskMembers_SkipsMemberWithoutPlatformInfoOrActivity() {
        // Given
        List<Long> memberIds = List.of(1L, 2L, 3L);
        when(askMemberId.getAllAskMembers(false)).thenReturn(memberIds);
        when(memberRepository.findAllByIdInWithCareers(memberIds))
                .thenReturn(List.of(askMember(1L), askMember(2L), askMember(3L)));
        InternalUserDetails noActivityUser = new InternalUserDetails(
                3L, "멤버3", null, null, null, null, 37, List.of());
        when(platformService.getInternalUsers(memberIds)).thenReturn(List.of(
                askUserDetails(1L),
                noActivityUser
        ));

        // When
        AskMemberResponse response = memberService.getAskMembers(null);

        // Then
        assertThat(response.members())
                .extracting(AskMemberResponse.QuestionTargetMember::id)
                .containsExactly(1L);
        verify(platformService, times(1)).getInternalUsers(memberIds);
        verify(platformService, never()).getInternalUser(any());
        verify(platformService, never()).getInternalUserDetailsMap(any());
    }

    @Test
    @DisplayName("DB에 대상 회원이 하나도 없으면 플랫폼을 호출하지 않는다")
    void getAskMembers_SkipsPlatformCallWhenNoMemberFound() {
        // Given
        List<Long> memberIds = List.of(1L);
        when(askMemberId.getAllAskMembers(false)).thenReturn(memberIds);
        when(memberRepository.findAllByIdInWithCareers(memberIds)).thenReturn(List.of());

        // When
        AskMemberResponse response = memberService.getAskMembers(null);

        // Then
        assertThat(response.members()).isEmpty();
        verifyNoInteractions(platformService);
    }

    private Member askMember(Long id) {
        return Member.builder()
                .id(id)
                .introduction("소개" + id)
                .build();
    }

    private InternalUserDetails askUserDetails(Long id) {
        return new InternalUserDetails(
                id,
                "멤버" + id,
                "image" + id,
                null,
                null,
                null,
                37,
                List.of(new SoptActivity(1, 37, "서버", null, null, true))
        );
    }
}
