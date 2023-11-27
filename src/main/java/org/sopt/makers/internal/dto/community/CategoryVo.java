package org.sopt.makers.internal.dto.community;

public record CategoryVo(
        Long id,
        String name,
        Long parentId,
        String parentCategoryName
) {}