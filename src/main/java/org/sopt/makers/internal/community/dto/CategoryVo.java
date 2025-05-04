package org.sopt.makers.internal.community.dto;

public record CategoryVo(
        Long id,
        String name,
        Long parentId,
        String parentCategoryName
) {}