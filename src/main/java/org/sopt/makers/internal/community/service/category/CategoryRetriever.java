package org.sopt.makers.internal.community.service.category;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.domain.category.Category;
import org.sopt.makers.internal.community.repository.category.CategoryRepository;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CategoryRetriever {

    private final CategoryRepository categoryRepository;

    public List<Category> getAllCategories() {

        return categoryRepository.findAll();
    }

    public void checkExistsCategoryById(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new NotFoundDBEntityException("존재하지 않는 category id 값입니다.");
        }
    }
}
