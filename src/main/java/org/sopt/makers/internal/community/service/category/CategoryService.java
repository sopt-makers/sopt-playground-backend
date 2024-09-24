package org.sopt.makers.internal.community.service.category;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.controller.dto.response.CommunityCategoryResponse;
import org.sopt.makers.internal.community.domain.category.Category;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRetriever categoryRetriever;

    public List<CommunityCategoryResponse> getAllCategoriesWithChildren() {

        List<Category> categories = categoryRetriever.getAllCategories();

        return categories.stream()
                .filter(category -> category.getParent() == null)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private CommunityCategoryResponse mapToResponse(Category category) {
        List<CommunityCategoryResponse> children = category.getChildren().stream()
                .map(this::mapToResponse)
                .toList();

        return CommunityCategoryResponse.of(category, children);
    }
}
