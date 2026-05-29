package org.sopt.makers.internal.community.service.category;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.domain.category.Category;
import org.sopt.makers.internal.community.dto.response.CommunityCategoryResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private static final Comparator<Category> CATEGORY_DISPLAY_ORDER_COMPARATOR =
        Comparator.comparing(
                Category::getDisplayOrder,
                Comparator.nullsLast(Integer::compareTo)
            )
            .thenComparing(Category::getId);

    private final CategoryRetriever categoryRetriever;

    @Transactional(readOnly = true)
    public List<CommunityCategoryResponse> getAllCategoriesWithChildren() {
        List<Category> activeCategories = categoryRetriever.findAllActiveCategoriesWithParent();

        Map<Long, List<Category>> childCategoriesByParentId = activeCategories.stream()
            .filter(category -> category.getParent() != null)
            .collect(Collectors.groupingBy(category -> category.getParent().getId()));

        return activeCategories.stream()
            .filter(category -> category.getParent() == null)
            .sorted(CATEGORY_DISPLAY_ORDER_COMPARATOR)
            .map(category -> CommunityCategoryResponse.from(category, childCategoriesByParentId))
            .toList();
    }
}