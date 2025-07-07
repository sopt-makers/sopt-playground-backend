package org.sopt.makers.internal.vote.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.makers.internal.community.domain.CommunityPost;
import org.sopt.makers.internal.community.service.post.CommunityPostRetriever;
import org.sopt.makers.internal.exception.ClientBadRequestException;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.service.MemberRetriever;
import org.sopt.makers.internal.vote.domain.Vote;
import org.sopt.makers.internal.vote.domain.VoteOption;
import org.sopt.makers.internal.vote.domain.VoteSelection;
import org.sopt.makers.internal.vote.dto.request.VoteRequest;
import org.sopt.makers.internal.vote.dto.response.VoteOptionResponse;
import org.sopt.makers.internal.vote.dto.response.VoteResponse;
import org.sopt.makers.internal.vote.repository.VoteOptionRepository;
import org.sopt.makers.internal.vote.repository.VoteRepository;
import org.sopt.makers.internal.vote.repository.VoteSelectionRepository;
import org.webjars.NotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VoteServiceTest {

    @Mock VoteRepository voteRepository;
    @Mock VoteOptionRepository voteOptionRepository;
    @Mock CommunityPostRetriever communityPostRetriever;
    @Mock VoteSelectionRepository voteSelectionRepository;
    @Mock MemberRetriever memberRetriever;

    @InjectMocks
    VoteService voteService;

    Member member;
    CommunityPost post;

    @BeforeEach
    void setUp() {
        member = mock(Member.class);
        post = mock(CommunityPost.class);
    }

    @Nested
    @DisplayName("투표 생성")
    class CreateVote {

        @Test
        @DisplayName("성공: 옵션 3개, isMultiple true")
        void createVote_success_multiple() {
            VoteRequest req = new VoteRequest(true, List.of("A", "B", "C"));

            voteService.createVote(post, req);

            ArgumentCaptor<Vote> voteCaptor = ArgumentCaptor.forClass(Vote.class);
            verify(voteRepository).save(voteCaptor.capture());
            Vote saved = voteCaptor.getValue();

            assertThat(saved.getVoteOptions()).hasSize(3);
            assertThat(saved.isMultipleOptions()).isTrue();
        }

        @Test
        @DisplayName("실패: 옵션 1개")
        void createVote_fail_optionsLessThan2() {
            VoteRequest req = new VoteRequest(true, List.of("A"));
            assertThatThrownBy(() -> voteService.createVote(post, req))
                    .isInstanceOf(ClientBadRequestException.class)
                    .hasMessageContaining("투표 옵션은 2개 이상");
        }

        @Test
        @DisplayName("실패: 금지 카테고리(21L)")
        void createVote_fail_sopticle() {
            when(post.getCategoryId()).thenReturn(21L);
            VoteRequest req = new VoteRequest(false, List.of("A", "B"));
            assertThatThrownBy(() -> voteService.createVote(post, req))
                    .isInstanceOf(ClientBadRequestException.class)
                    .hasMessageContaining("솝티클 카테고리");
        }

        @Test
        @DisplayName("실패: 옵션 내용 40자 초과")
        void createVote_fail_optionTooLong() {
            String longOption = "A".repeat(41);
            VoteRequest req = new VoteRequest(false, List.of(longOption, "B"));
            assertThatThrownBy(() -> voteService.createVote(post, req))
                    .isInstanceOf(ClientBadRequestException.class)
                    .hasMessageContaining("40자까지만");
        }
    }

    @Nested
    @DisplayName("투표 선택")
    class SelectVote {

        Vote vote;
        VoteOption option1, option2;

        @BeforeEach
        void setUpVote() {
            vote = mock(Vote.class);
            option1 = mock(VoteOption.class);
            option2 = mock(VoteOption.class);
        }

        @Test
        @DisplayName("성공: 단일 선택")
        void selectVote_success_single() {
            Long postId = 1L, userId = 2L;
            when(communityPostRetriever.findCommunityPostById(postId)).thenReturn(post);
            when(voteRepository.findByPost(post)).thenReturn(Optional.of(vote));
            when(memberRetriever.findMemberById(userId)).thenReturn(member);
            when(voteOptionRepository.findAllById(List.of(1L))).thenReturn(List.of(option1));
            when(voteSelectionRepository.existsByVoteOptionInAndMember(any(), any())).thenReturn(false);

            voteService.selectVote(postId, userId, List.of(1L));

            verify(voteSelectionRepository, times(1)).save(any(VoteSelection.class));
            verify(option1, times(1)).increaseCount();
        }

        @Test
        @DisplayName("실패: 이미 투표함")
        void selectVote_fail_alreadyVoted() {
            Long postId = 1L, userId = 2L;
            when(communityPostRetriever.findCommunityPostById(postId)).thenReturn(post);
            when(voteRepository.findByPost(post)).thenReturn(Optional.of(vote));
            when(memberRetriever.findMemberById(userId)).thenReturn(member);
            when(voteOptionRepository.findAllById(List.of(1L))).thenReturn(List.of(option1));
            when(voteSelectionRepository.existsByVoteOptionInAndMember(any(), any())).thenReturn(true);

            assertThatThrownBy(() -> voteService.selectVote(postId, userId, List.of(1L)))
                    .isInstanceOf(ClientBadRequestException.class)
                    .hasMessageContaining("이미 투표했습니다.");
        }

        @Test
        @DisplayName("실패: 복수 선택 불가인데 2개 선택")
        void selectVote_fail_multiSelectNotAllowed() {
            Long postId = 1L, userId = 2L;
            when(communityPostRetriever.findCommunityPostById(postId)).thenReturn(post);
            when(voteRepository.findByPost(post)).thenReturn(Optional.of(vote));
            when(memberRetriever.findMemberById(userId)).thenReturn(member);
            when(voteOptionRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(option1, option2));
            when(voteSelectionRepository.existsByVoteOptionInAndMember(any(), any())).thenReturn(false);

            assertThatThrownBy(() -> voteService.selectVote(postId, userId, List.of(1L, 2L)))
                    .isInstanceOf(ClientBadRequestException.class)
                    .hasMessageContaining("복수 선택 불가능");
        }

        @Test
        @DisplayName("실패: 옵션ID 중 일부가 존재하지 않음")
        void selectVote_fail_optionNotFound() {
            Long postId = 1L, userId = 2L;
            when(communityPostRetriever.findCommunityPostById(postId)).thenReturn(post);
            when(voteRepository.findByPost(post)).thenReturn(Optional.of(vote));
            when(memberRetriever.findMemberById(userId)).thenReturn(member);
            when(vote.isMultipleOptions()).thenReturn(true);
            when(voteOptionRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(option1));
            when(voteSelectionRepository.existsByVoteOptionInAndMember(any(), any())).thenReturn(false);

            assertThatThrownBy(() -> voteService.selectVote(postId, userId, List.of(1L, 2L)))
                    .isInstanceOf(ClientBadRequestException.class)
                    .hasMessageContaining("존재하지 않는 투표 옵션");
        }

        @Test
        @DisplayName("실패: 게시글 없음")
        void selectVote_fail_postNotFound() {
            when(communityPostRetriever.findCommunityPostById(anyLong()))
                    .thenThrow(new NotFoundException("게시글이 존재하지 않습니다."));
            assertThatThrownBy(() -> voteService.selectVote(1L, 2L, List.of(1L)))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("게시글이 존재하지 않습니다.");
        }

        @Test
        @DisplayName("실패: 유저 없음")
        void selectVote_fail_userNotFound() {
            when(communityPostRetriever.findCommunityPostById(anyLong())).thenReturn(post);
            when(memberRetriever.findMemberById(anyLong()))
                    .thenThrow(new NotFoundException("유저가 존재하지 않습니다."));
            assertThatThrownBy(() -> voteService.selectVote(1L, 2L, List.of(1L)))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("유저가 존재하지 않습니다.");
        }
    }

    @Nested
    @DisplayName("투표 조회")
    class GetVote {

        Vote vote;
        VoteOption option1, option2;
        VoteSelection voteSelection1, voteSelection2;
        CommunityPost post;
        Member member;
        Long postId = 1L, userId = 2L;

        @BeforeEach
        void setUpVote() {
            // mock 객체
            vote = mock(Vote.class);
            option1 = mock(VoteOption.class);
            option2 = mock(VoteOption.class);
            post = mock(CommunityPost.class);
            member = mock(Member.class);
            voteSelection1 = mock(VoteSelection.class);
            voteSelection2 = mock(VoteSelection.class);

            // vote와 voteOption 설정
            when(vote.getVoteOptions()).thenReturn(new ArrayList<>(List.of(option1, option2)));
            when(vote.getId()).thenReturn(10L);
            when(option1.getId()).thenReturn(20L);
            when(option1.getContent()).thenReturn("치킨");
            when(option1.getVoteCount()).thenReturn(6);
            when(option2.getId()).thenReturn(21L);
            when(option2.getContent()).thenReturn("피자");
            when(option2.getVoteCount()).thenReturn(4);

            // 리포지토리 응답 정의
            when(communityPostRetriever.findCommunityPostById(postId)).thenReturn(post);
            when(voteRepository.findByPost(post)).thenReturn(Optional.of(vote));
            when(memberRetriever.findMemberById(userId)).thenReturn(member);

            when(voteSelectionRepository.countDistinctMembersByVote(vote)).thenReturn(7);
        }

        @Test
        @DisplayName("성공: 다중 투표의 투표 조회")
        void getVote_success_multiple() {
            // 다중 투표 설정
            when(vote.isMultipleOptions()).thenReturn(true);
            when(voteSelectionRepository.findByVoteOptionInAndMember(anyList(), eq(member)))
                    .thenReturn(List.of(voteSelection1, voteSelection2));
            when(voteSelection1.getVoteOption()).thenReturn(option1);
            when(voteSelection2.getVoteOption()).thenReturn(option2);

            // 테스트 실행
            VoteResponse response = voteService.getVoteByPostId(postId, userId);

            // 검증
            assertThat(response).isNotNull();
            assertThat(response.hasVoted()).isTrue();
            assertThat(response.totalParticipants()).isEqualTo(7);
            assertThat(response.options()).hasSize(2);

            VoteOptionResponse chickenOption = response.options().get(0);
            assertThat(chickenOption.id()).isEqualTo(20L);
            assertThat(chickenOption.content()).isEqualTo("치킨");
            assertThat(chickenOption.voteCount()).isEqualTo(6);
            assertThat(chickenOption.votePercent()).isEqualTo(60);
            assertThat(chickenOption.isSelected()).isTrue();

            VoteOptionResponse pizzaOption = response.options().get(1);
            assertThat(pizzaOption.id()).isEqualTo(21L);
            assertThat(pizzaOption.content()).isEqualTo("피자");
            assertThat(pizzaOption.voteCount()).isEqualTo(4);
            assertThat(pizzaOption.votePercent()).isEqualTo(40);
            assertThat(pizzaOption.isSelected()).isTrue();
        }

        @Test
        @DisplayName("성공: 단일 투표의 투표 조회")
        void getVote_success_single() {
            // 단일 투표 설정
            when(vote.isMultipleOptions()).thenReturn(false);
            when(voteSelectionRepository.findByVoteOptionInAndMember(anyList(), eq(member)))
                    .thenReturn(List.of(voteSelection1));
            when(voteSelection1.getVoteOption()).thenReturn(option1);

            // 테스트 실행
            VoteResponse response = voteService.getVoteByPostId(postId, userId);

            // 검증
            assertThat(response).isNotNull();
            assertThat(response.hasVoted()).isTrue();
            assertThat(response.totalParticipants()).isEqualTo(7);
            assertThat(response.options()).hasSize(2);

            VoteOptionResponse chickenOption = response.options().get(0);
            assertThat(chickenOption.id()).isEqualTo(20L);
            assertThat(chickenOption.content()).isEqualTo("치킨");
            assertThat(chickenOption.voteCount()).isEqualTo(6);
            assertThat(chickenOption.votePercent()).isEqualTo(60);
            assertThat(chickenOption.isSelected()).isTrue();

            VoteOptionResponse pizzaOption = response.options().get(1);
            assertThat(pizzaOption.id()).isEqualTo(21L);
            assertThat(pizzaOption.content()).isEqualTo("피자");
            assertThat(pizzaOption.voteCount()).isEqualTo(4);
            assertThat(pizzaOption.votePercent()).isEqualTo(40);
            assertThat(pizzaOption.isSelected()).isFalse();
        }
    }
}
