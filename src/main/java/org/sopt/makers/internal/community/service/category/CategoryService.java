package org.sopt.makers.internal.community.service.category;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.dto.response.CommunityCategoryResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRetriever categoryRetriever;

    public List<CommunityCategoryResponse> getAllCategoriesWithChildren() {
        return categoryRetriever.findActiveRootListCategories().stream()
            .map(CommunityCategoryResponse::fromRoot)
            .toList();
    }
}
