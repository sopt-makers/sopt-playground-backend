package org.sopt.makers.internal.service.community.category;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.makers.internal.community.controller.dto.response.CommunityCategoryResponse;
import org.sopt.makers.internal.community.domain.category.Category;
import org.sopt.makers.internal.community.service.category.CategoryRetriever;
import org.sopt.makers.internal.community.service.category.CategoryService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceUnitTest {

    @InjectMocks
    private CategoryService categoryService;

    @Mock
    private CategoryRetriever categoryRetriever;

    @Test
    @DisplayName("커뮤니티 카테고리 조회")
    public void testGetAllCategoriesWithChildren() {

        // Given
        Category parentOneCategory = new Category(
                1L,
                "Parent One Category",
                "This Category is parent one category",
                true,
                true,
                true,
                null,
                new ArrayList<>()
        );

        Category parentTwoCategory = new Category(
                2L,
                "Parent Two Category",
                "This Category is parent two category",
                true,
                true,
                true,
                null,
                new ArrayList<>()
        );

        Category childrenCategory = new Category(
                3L,
                "Child Category",
                "This Category is child category",
                true,
                true,
                true,
                parentOneCategory,
                new ArrayList<>()
        );

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
