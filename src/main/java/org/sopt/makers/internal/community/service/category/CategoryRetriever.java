package org.sopt.makers.internal.community.service.category;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.domain.category.Category;
import org.sopt.makers.internal.community.domain.enums.CommunityCategoryCode;
import org.sopt.makers.internal.community.repository.category.CategoryRepository;
import org.sopt.makers.internal.exception.NotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CategoryRetriever {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public Category findActiveCategoryByCode(CommunityCategoryCode code) {
        return categoryRepository.findByCodeAndIsActiveTrue(code)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 category code 값입니다."));
    }

    @Transactional(readOnly = true)
    public List<Category> findActiveRootListCategories() {
        return categoryRepository.findAllByIsActiveTrueAndParentIsNullAndCodeInOrderByDisplayOrderAsc(
            List.of(
                CommunityCategoryCode.FREE,
                CommunityCategoryCode.PROMOTION,
                CommunityCategoryCode.SOPTICLE
            )
        );
    }

    @Transactional(readOnly = true)
    public List<Category> findActiveCategoriesByCodes(List<CommunityCategoryCode> codes) {
        return categoryRepository.findAllByCodeInAndIsActiveTrue(codes);
    }
}
