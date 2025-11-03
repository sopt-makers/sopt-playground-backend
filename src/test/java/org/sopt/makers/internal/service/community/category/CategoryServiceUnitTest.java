package org.sopt.makers.internal.service.community.category;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.makers.internal.community.dto.response.CommunityCategoryResponse;
import org.sopt.makers.internal.community.domain.category.Category;
import org.sopt.makers.internal.community.mapper.CommunityMapper;
import org.sopt.makers.internal.community.mapper.CommunityResponseMapper;
import org.sopt.makers.internal.community.repository.CommunityQueryRepository;
import org.sopt.makers.internal.community.repository.ReportPostRepository;
import org.sopt.makers.internal.community.repository.comment.CommunityCommentRepository;
import org.sopt.makers.internal.community.repository.comment.DeletedCommunityCommentRepository;
import org.sopt.makers.internal.community.repository.post.CommunityPostLikeRepository;
import org.sopt.makers.internal.community.repository.post.CommunityPostRepository;
import org.sopt.makers.internal.community.repository.post.DeletedCommunityPostRepository;
import org.sopt.makers.internal.community.service.SopticleScrapedService;
import org.sopt.makers.internal.community.service.category.CategoryRetriever;
import org.sopt.makers.internal.community.service.category.CategoryService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.sopt.makers.internal.community.service.post.CommunityPostModifier;
import org.sopt.makers.internal.community.service.post.CommunityPostRetriever;
import org.sopt.makers.internal.external.pushNotification.PushNotificationService;
import org.sopt.makers.internal.external.slack.SlackClient;
import org.sopt.makers.internal.external.slack.SlackMessageUtil;
import org.sopt.makers.internal.member.repository.MemberBlockRepository;
import org.sopt.makers.internal.member.service.MemberRetriever;
import org.sopt.makers.internal.vote.service.VoteService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceUnitTest {

    @InjectMocks
    private CategoryService categoryService;
    @Mock private CategoryRetriever categoryRetriever;

    @Test
    @DisplayName("커뮤니티 카테고리 조회")
    public void testGetAllCategoriesWithChildren() {

        // Given
        Category parentOneCategory = Category.builder()
                .id(1L)
                .name("Parent One Category")
                .content("This Category is parent one category")
                .hasAll(true)
                .hasBlind(true)
                .hasQuestion(true)
                .parent(null)
                .children(new ArrayList<>())
                .displayOrder(1)
                .build();

        Category parentTwoCategory = Category.builder()
                .id(2L)
                .name("Parent Two Category")
                .content("This Category is parent two category")
                .hasAll(true)
                .hasBlind(true)
                .hasQuestion(true)
                .parent(null)
                .children(new ArrayList<>())
                .displayOrder(2)
                .build();

        Category childrenCategory = Category.builder()
                .id(3L)
                .name("Child Category")
                .content("This Category is child category")
                .hasAll(true)
                .hasBlind(true)
                .hasQuestion(true)
                .parent(null)  // Spring Boot 3: enableAssociationManagement로 인해 parent 설정 시 자동 추가되므로 null로 설정
                .children(new ArrayList<>())
                .displayOrder(3)
                .build();

        parentOneCategory.getChildren().add(childrenCategory);

        when(categoryRetriever.getAllCategories()).thenReturn(Arrays.asList(parentOneCategory, parentTwoCategory));

        // Given
        List<CommunityCategoryResponse> responses = categoryService.getAllCategoriesWithChildren();

        // Then
        assertEquals(2, responses.size());

        CommunityCategoryResponse response = responses.get(0);
        assertEquals(1L, response.id());
        assertEquals("Parent One Category", response.name());
        assertEquals("This Category is parent one category", response.content());
        assertEquals(true, response.hasAll());
        assertEquals(true, response.hasBlind());
        assertEquals(true, response.hasQuestion());
        assertEquals(1, response.children().size());

        CommunityCategoryResponse childResponse = response.children().get(0);
        assertEquals(3L, childResponse.id());
        assertEquals("Child Category", childResponse.name());
        assertEquals("This Category is child category", childResponse.content());
        assertEquals(true, childResponse.hasAll());
        assertEquals(true, childResponse.hasBlind());
        assertEquals(true, childResponse.hasQuestion());
    }
}
