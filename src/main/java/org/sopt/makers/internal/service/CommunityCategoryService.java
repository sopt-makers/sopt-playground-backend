package org.sopt.makers.internal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.makers.internal.domain.community.Category;
import org.sopt.makers.internal.dto.community.CategoryDto;
import org.sopt.makers.internal.repository.community.CategoryQueryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class CommunityCategoryService {

    private final CategoryQueryRepository categoryQueryRepository;

    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategory() {
        List<Category> categories = categoryQueryRepository.getCategoryList();
        return CategoryDto.toDtoList(categories);
    }
}
