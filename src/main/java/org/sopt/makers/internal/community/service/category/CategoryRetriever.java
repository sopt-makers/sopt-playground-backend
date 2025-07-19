package org.sopt.makers.internal.community.service.category;

import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.domain.category.Category;
import org.sopt.makers.internal.community.repository.category.CategoryRepository;
import org.sopt.makers.internal.exception.ClientBadRequestException;
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

    public List<Category> findAllByIds(List<Long> ids) {
        return categoryRepository.findAllById(ids);
    }

    public List<Long> findAllDescendantIds(Long parentId) {
        List<Category> allCategories = categoryRepository.findAll();
        Category parent = allCategories.stream()
                .filter(c -> c.getId().equals(parentId))
                .findFirst()
                .orElseThrow(() -> new ClientBadRequestException("존재하지 않는 카테고리입니다."));

        List<Long> descendantIds = new ArrayList<>();
        collectDescendantIds(parent, descendantIds, allCategories);
        return descendantIds;
    }

    private void collectDescendantIds(Category category, List<Long> ids, List<Category> allCategories) {
        ids.add(category.getId());
        allCategories.stream()
                .filter(c -> category.getId().equals(c.getParent() != null ? c.getParent().getId() : null))
                .forEach(child -> collectDescendantIds(child, ids, allCategories));
    }
}
