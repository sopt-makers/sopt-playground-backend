package org.sopt.makers.internal.community.dto;

import org.springframework.aot.hint.annotation.Reflective;

@Reflective
public record CategoryVo(
        Long id,
        String name,
        Long parentId,
        String parentCategoryName
) {}