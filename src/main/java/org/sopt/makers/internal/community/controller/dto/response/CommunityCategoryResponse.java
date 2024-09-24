package org.sopt.makers.internal.community.controller.dto.response;

import org.sopt.makers.internal.community.domain.category.Category;

import java.util.List;

public record CommunityCategoryResponse(

        Long id,

        String name,

        String content,

        Boolean hasAll,

        Boolean hasBlind,

        Boolean hasQuestion,

        List<CommunityCategoryResponse> children
) {

    public static CommunityCategoryResponse of(Category category, List<CommunityCategoryResponse> children) {
        return new CommunityCategoryResponse(
                category.getId(),
                category.getName(),
                category.getContent(),
                category.getHasAll(),
                category.getHasBlind(),
                category.getHasQuestion(),
                children
        );
    }
}
