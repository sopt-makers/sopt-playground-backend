package org.sopt.makers.internal.community.service.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.makers.internal.community.domain.CommunityPost;
import org.sopt.makers.internal.community.domain.category.Category;
import org.sopt.makers.internal.community.dto.response.RecentPostResponse;
import org.sopt.makers.internal.community.mapper.CommunityResponseMapper;
import org.sopt.makers.internal.community.repository.comment.CommunityCommentRepository;
import org.sopt.makers.internal.community.repository.post.CommunityPostLikeRepository;
import org.sopt.makers.internal.community.repository.post.CommunityPostRepository;
import org.sopt.makers.internal.community.service.category.CategoryRetriever;
import org.sopt.makers.internal.vote.service.VoteService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
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
    private CategoryRetriever categoryRetriever;
    @Mock
    private CommunityPostRepository communityPostRepository;
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
        @DisplayName("성공 - 게시글(부모/자식 카테고리)이 존재할 경우, DTO 리스트를 올바르게 반환한다")
        void getRecentPosts_Success_WhenPostsExist() {
            // Given
            // 1. Mock 카테고리 데이터 (부모-자식 관계)
            Category parentCategory = Category.builder().id(1L).name("메인").build();
            Category childCategory = Category.builder().id(2L).name("자유").parent(parentCategory).build();
            List<Category> mockCategories = List.of(parentCategory, childCategory);

            // 2. Mock 게시글 데이터
            CommunityPost postInParent = CommunityPost.builder().id(101L).categoryId(1L).title("부모 카테고리 글").content("부모글 내용").build();
            CommunityPost postInChild = CommunityPost.builder().id(102L).categoryId(2L).title("자식 카테고리 글").content("자식글 내용").build();
            List<CommunityPost> mockPosts = List.of(postInParent, postInChild);

            ReflectionTestUtils.setField(postInParent, "createdAt", LocalDateTime.now());
            ReflectionTestUtils.setField(postInChild, "createdAt", LocalDateTime.now().minusMinutes(5));

            when(communityPostRepository.findTop5ByCategoryIdNotOrderByCreatedAtDesc(SOPTICLE_CATEGORY_ID)).thenReturn(mockPosts);
            when(categoryRetriever.findAllByIds(anyList())).thenReturn(mockCategories);

            // 3. Count 및 Vote 데이터 Mocking
            when(communityPostLikeRepository.countAllByPostId(anyLong())).thenReturn(10);
            when(communityCommentRepository.countAllByPostId(anyLong())).thenReturn(5);
            when(voteService.getVoteByPostId(anyLong(), anyLong())).thenReturn(null);

            // 4. Mapper가 반환할 최종 DTO Mocking
            RecentPostResponse response1 = new RecentPostResponse(101L, "부모 카테고리 글", "부모글 내용", "방금 전", 10, 5, 1L, "메인", null, true);
            RecentPostResponse response2 = new RecentPostResponse(102L, "자식 카테고리 글", "자식글 내용", "5분 전", 10, 5, 1L, "메인", null, true); // categoryId는 부모 ID(1L)

            when(communityResponseMapper.toRecentPostResponse(postInParent, 10, 5, 1L, "메인", null)).thenReturn(response1);
            when(communityResponseMapper.toRecentPostResponse(postInChild, 10, 5, 1L, "메인", null)).thenReturn(response2);

            // When
            List<RecentPostResponse> result = communityPostService.getRecentPosts(MEMBER_ID);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).categoryId()).isEqualTo(1L);
            assertThat(result.get(0).categoryName()).isEqualTo("메인");
            assertThat(result.get(1).categoryId()).isEqualTo(1L);
            assertThat(result.get(1).categoryName()).isEqualTo("메인");

            // Verify
            verify(categoryRetriever, times(1)).findAllByIds(anyList());
            verify(communityResponseMapper, times(1)).toRecentPostResponse(postInParent, 10, 5, 1L, "메인", null);
            verify(communityResponseMapper, times(1)).toRecentPostResponse(postInChild, 10, 5, 1L, "메인", null);
        }

        @Test
        @DisplayName("성공 - 조회된 게시글이 없을 경우, 빈 리스트를 반환한다")
        void getRecentPosts_Success_WhenPostsDoNotExist() {
            // Given
            when(communityPostRepository.findTop5ByCategoryIdNotOrderByCreatedAtDesc(SOPTICLE_CATEGORY_ID)).thenReturn(Collections.emptyList());

            // When
            List<RecentPostResponse> result = communityPostService.getRecentPosts(MEMBER_ID);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();

            // Verify
            verify(categoryRetriever, never()).findAllByIds(Collections.emptyList());
            verify(voteService, never()).getVoteByPostId(anyLong(), anyLong());
            verify(communityResponseMapper, never()).toRecentPostResponse(any(), anyInt(), anyInt(), anyLong(), any(), any());
        }
    }
}