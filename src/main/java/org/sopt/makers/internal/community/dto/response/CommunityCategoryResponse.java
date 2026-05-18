package org.sopt.makers.internal.community.dto.response;

import java.util.List;
import org.sopt.makers.internal.community.domain.category.Category;
import org.sopt.makers.internal.community.domain.enums.CommunityPostListFilter;

public record CommunityCategoryResponse(
    String code,
    String name,
    String content,
    Boolean hasBlind,
    List<CommunityCategoryResponse> children
) {

    public static CommunityCategoryResponse fromRoot(Category category) {
        List<CommunityCategoryResponse> children = switch (category.getCode()) {
            case FREE -> List.of();
            case PROMOTION -> promotionFilters();
            case SOPTICLE -> sopticleFilters();
            default -> List.of();
        };

        return new CommunityCategoryResponse(
            category.getCode().name(),
            category.getName(),
            category.getContent(),
            category.getHasBlind(),
            children
        );
    }

    private static List<CommunityCategoryResponse> promotionFilters() {
        return List.of(
            filter(CommunityPostListFilter.ALL, "전체"),
            filter(CommunityPostListFilter.EVENT, "행사"),
            filter(CommunityPostListFilter.PROJECT, "프로젝트"),
            filter(CommunityPostListFilter.RECRUIT, "채용"),
            filter(CommunityPostListFilter.ETC, "기타")
        );
    }

    private static List<CommunityCategoryResponse> sopticleFilters() {
        return List.of(
            filter(CommunityPostListFilter.ALL, "전체"),
            filter(CommunityPostListFilter.PLAN, "기획"),
            filter(CommunityPostListFilter.DESIGN, "디자인"),
            filter(CommunityPostListFilter.SERVER, "서버"),
            filter(CommunityPostListFilter.WEB, "웹"),
            filter(CommunityPostListFilter.IOS, "iOS"),
            filter(CommunityPostListFilter.ANDROID, "Android"),
            filter(CommunityPostListFilter.ETC, "기타")
        );
    }

    private static CommunityCategoryResponse filter(CommunityPostListFilter filter, String name) {
        return new CommunityCategoryResponse(
            filter.name(),
            name,
            null,
            false,
            List.of()
        );
    }
}