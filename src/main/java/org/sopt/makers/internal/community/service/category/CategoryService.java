package org.sopt.makers.internal.community.service.category;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.dto.response.CommunityCategoryResponse;
import org.sopt.makers.internal.community.domain.category.Category;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRetriever categoryRetriever;

    public List<CommunityCategoryResponse> getAllCategoriesWithChildren() {

        List<Category> categories = categoryRetriever.getAllCategories();

        return categories.stream()
                .filter(category -> category.getParent() == null)
                .sorted(Comparator.comparing(Category::getDisplayOrder))
                .map(CommunityCategoryResponse::from)
                .toList();
    }
}
