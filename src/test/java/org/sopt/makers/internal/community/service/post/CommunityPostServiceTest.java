package org.sopt.makers.internal.community.service.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.makers.internal.community.domain.CommunityPost;
import org.sopt.makers.internal.community.dto.response.RecentPostResponse;
import org.sopt.makers.internal.community.mapper.CommunityResponseMapper;
import org.sopt.makers.internal.community.repository.CommunityQueryRepository;
import org.sopt.makers.internal.community.repository.comment.CommunityCommentRepository;
import org.sopt.makers.internal.community.repository.post.CommunityPostLikeRepository;
import org.sopt.makers.internal.community.repository.post.CommunityPostRepository;
import org.sopt.makers.internal.vote.dto.response.VoteResponse;
import org.sopt.makers.internal.vote.service.VoteService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommunityPostService 단위 테스트")
class CommunityPostServiceTest {

    @InjectMocks
    private CommunityPostService communityPostService;

    @Mock
    private CommunityPostRepository communityPostRepository;
    @Mock
    private CommunityQueryRepository communityQueryRepository;
    @Mock
    private CommunityPostLikeRepository communityPostLikeRepository;
    @Mock
    private CommunityCommentRepository communityCommentRepository;
    @Mock
    private VoteService voteService;
    @Mock
    private CommunityResponseMapper communityResponseMapper;

    private static final long SOPTICLE_CATEGORY_ID = 21L;

    @Nested
    @DisplayName("홈 최신 게시글 목록 조회 (getRecentPosts)")
    class GetRecentPosts {

        private final Long MEMBER_ID = 1L;

        @Test
        @DisplayName("성공 - 게시글(투표 포함/미포함)이 존재할 경우, DTO 리스트를 올바르게 반환한다")
        void getRecentPosts_Success_WhenPostsExist() {
            // Given
            // 1. Mock 게시글 데이터 생성
            CommunityPost postWithVote = CommunityPost.builder()
                    .id(101L)
                    .categoryId(1L)
                    .title("투표 있는 글")
                    .content("내용1")
                    .build();
            CommunityPost postWithoutVote = CommunityPost.builder()
                    .id(102L)
                    .categoryId(2L)
                    .title("투표 없는 글")
                    .content("내용2")
                    .build();

            // 1-2. Reflection을 사용하여 상속된 createdAt 필드 값 설정
            ReflectionTestUtils.setField(postWithVote, "createdAt", LocalDateTime.now());
            ReflectionTestUtils.setField(postWithoutVote, "createdAt", LocalDateTime.now().minusMinutes(5));

            List<CommunityPost> mockPosts = List.of(postWithVote, postWithoutVote);

            when(communityPostRepository.findTop5ByCategoryIdNotOrderByCreatedAtDesc(SOPTICLE_CATEGORY_ID))
                    .thenReturn(mockPosts);

            // 2. Mock 카테고리 이름 데이터
            Map<Long, String> categoryNameMap = Map.of(1L, "자유", 2L, "홍보");
            when(communityQueryRepository.getCategoryNamesByIds(anyList())).thenReturn(categoryNameMap);

            // 3. 각 게시글의 Count 및 Vote 데이터 Mocking
            // 3-1. 투표가 있는 게시글 (댓글 5개)
            when(communityPostLikeRepository.countAllByPostId(postWithVote.getId())).thenReturn(10);
            when(communityCommentRepository.countAllByPostId(postWithVote.getId())).thenReturn(5);
            VoteResponse voteResponse = new VoteResponse(1L, false, true, 15, Collections.emptyList());
            when(voteService.getVoteByPostId(postWithVote.getId(), MEMBER_ID)).thenReturn(voteResponse);

            // 3-2. 투표가 없는 게시글 (댓글 0개)
            when(communityPostLikeRepository.countAllByPostId(postWithoutVote.getId())).thenReturn(3);
            when(communityCommentRepository.countAllByPostId(postWithoutVote.getId())).thenReturn(0);
            when(voteService.getVoteByPostId(postWithoutVote.getId(), MEMBER_ID)).thenReturn(null);

            // 4. Mapper가 반환할 최종 DTO Mocking
            RecentPostResponse response1 = new RecentPostResponse(101L, "투표 있는 글", "내용1", "방금 전", 10, 5, 1L,"자유", 15, true);
            RecentPostResponse response2 = new RecentPostResponse(102L, "투표 없는 글", "내용2", "1분 전", 3, 0, 2L, "홍보", null, false);

            when(communityResponseMapper.toRecentPostResponse(postWithVote, 10, 5, 1L, "자유", 15)).thenReturn(response1);
            when(communityResponseMapper.toRecentPostResponse(postWithoutVote, 3, 0, 2L, "홍보", null)).thenReturn(response2);

            // When
            List<RecentPostResponse> result = communityPostService.getRecentPosts(MEMBER_ID);

            // Then
            // 1. 결과 검증
            assertThat(result).hasSize(2);
            assertThat(result.get(0)).isEqualTo(response1);
            assertThat(result.get(1)).isEqualTo(response2);

            assertThat(result.get(0).totalVoteCount()).isEqualTo(15);
            assertThat(result.get(0).isAnswered()).isTrue();

            assertThat(result.get(1).totalVoteCount()).isNull();
            assertThat(result.get(1).isAnswered()).isFalse();

            // 2. Mock 객체 상호작용 검증
            verify(communityPostRepository, times(1)).findTop5ByCategoryIdNotOrderByCreatedAtDesc(SOPTICLE_CATEGORY_ID);
            verify(communityQueryRepository, times(1)).getCategoryNamesByIds(anyList());
            verify(voteService, times(1)).getVoteByPostId(postWithVote.getId(), MEMBER_ID);
            verify(voteService, times(1)).getVoteByPostId(postWithoutVote.getId(), MEMBER_ID);
            verify(communityResponseMapper, times(1)).toRecentPostResponse(postWithVote, 10, 5, 1L,"자유", 15);
            verify(communityResponseMapper, times(1)).toRecentPostResponse(postWithoutVote, 3, 0, 2L, "홍보", null);
        }

        @Test
        @DisplayName("성공 - 조회된 게시글이 없을 경우, 빈 리스트를 반환한다")
        void getRecentPosts_Success_WhenPostsDoNotExist() {
            // Given
            when(communityPostRepository.findTop5ByCategoryIdNotOrderByCreatedAtDesc(SOPTICLE_CATEGORY_ID))
                    .thenReturn(Collections.emptyList());

            // When
            List<RecentPostResponse> result = communityPostService.getRecentPosts(MEMBER_ID);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();

            // 게시글이 없으므로 다른 의존성은 호출되지 않아야 함
            verify(communityQueryRepository, times(1)).getCategoryNamesByIds(Collections.emptyList());
            verify(voteService, never()).getVoteByPostId(anyLong(), anyLong());
            verify(communityResponseMapper, never()).toRecentPostResponse(any(), anyInt(), anyInt(), anyLong(), any(), any());
        }
    }
}