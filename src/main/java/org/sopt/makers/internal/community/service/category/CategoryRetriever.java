package org.sopt.makers.internal.community.service.category;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.domain.category.Category;
import org.sopt.makers.internal.community.repository.category.CategoryRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CategoryRetriever {

    private final CategoryRepository categoryRepository;

    public List<Category> getAllCategories() {

        return categoryRepository.findAll();
    }
}
