package org.sopt.makers.internal.community.dto.response;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.sopt.makers.internal.community.domain.category.Category;
import org.sopt.makers.internal.community.domain.enums.CommunityCategoryCode;
import org.sopt.makers.internal.community.domain.enums.CommunityPostListFilter;

public record CommunityCategoryResponse(
    String code,
    String name,
    String content,
    Boolean hasBlind,
    List<CommunityCategoryResponse> children
) {

    private static final Comparator<Category> CATEGORY_DISPLAY_ORDER_COMPARATOR =
        Comparator.comparing(
                Category::getDisplayOrder,
                Comparator.nullsLast(Integer::compareTo)
            )
            .thenComparing(Category::getId);

    public static CommunityCategoryResponse from(
        Category category,
        Map<Long, List<Category>> childCategoriesByParentId
    ) {
        return new CommunityCategoryResponse(
            toResponseCode(category.getCode()),
            category.getName(),
            category.getContent(),
            category.getHasBlind(),
            toChildren(category, childCategoriesByParentId)
        );
    }

    private static List<CommunityCategoryResponse> toChildren(
        Category category,
        Map<Long, List<Category>> childCategoriesByParentId
    ) {
        return childCategoriesByParentId
            .getOrDefault(category.getId(), List.of())
            .stream()
            .sorted(CATEGORY_DISPLAY_ORDER_COMPARATOR)
            .map(childCategory -> CommunityCategoryResponse.from(childCategory, childCategoriesByParentId))
            .toList();
    }

    private static String toResponseCode(CommunityCategoryCode code) {
        return switch (code) {
            case PROMOTION_EVENT -> CommunityPostListFilter.EVENT.name();
            case PROMOTION_PROJECT -> CommunityPostListFilter.PROJECT.name();
            case PROMOTION_RECRUIT -> CommunityPostListFilter.RECRUIT.name();
            case PROMOTION_ETC -> CommunityPostListFilter.ETC.name();

            case SOPTICLE_PLAN -> CommunityPostListFilter.PLAN.name();
            case SOPTICLE_DESIGN -> CommunityPostListFilter.DESIGN.name();
            case SOPTICLE_SERVER -> CommunityPostListFilter.SERVER.name();
            case SOPTICLE_WEB -> CommunityPostListFilter.WEB.name();
            case SOPTICLE_IOS -> CommunityPostListFilter.IOS.name();
            case SOPTICLE_ANDROID -> CommunityPostListFilter.ANDROID.name();
            case SOPTICLE_ETC -> CommunityPostListFilter.ETC.name();

            default -> code.name();
        };
    }
}