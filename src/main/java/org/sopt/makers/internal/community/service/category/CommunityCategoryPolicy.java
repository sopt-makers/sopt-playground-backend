package org.sopt.makers.internal.community.service.category;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sopt.makers.internal.community.domain.category.Category;
import org.sopt.makers.internal.community.domain.enums.CommunityCategoryCode;
import org.sopt.makers.internal.community.domain.enums.CommunityCategoryGroup;
import org.sopt.makers.internal.community.domain.enums.CommunityPostListCategory;
import org.sopt.makers.internal.community.domain.enums.CommunityPostListFilter;
import org.sopt.makers.internal.community.domain.enums.CommunityPostTag;
import org.sopt.makers.internal.exception.BadRequestException;
import org.springframework.stereotype.Component;

@Component
public class CommunityCategoryPolicy {

	private static final List<CommunityCategoryCode> PROMOTION_ALL_CODES = List.of(
		CommunityCategoryCode.PROMOTION,
		CommunityCategoryCode.PROMOTION_EVENT,
		CommunityCategoryCode.PROMOTION_PROJECT,
		CommunityCategoryCode.PROMOTION_RECRUIT,
		CommunityCategoryCode.PROMOTION_ETC
	);

	private static final List<CommunityCategoryCode> SOPTICLE_ALL_CODES = List.of(
		CommunityCategoryCode.SOPTICLE,
		CommunityCategoryCode.SOPTICLE_PLAN,
		CommunityCategoryCode.SOPTICLE_DESIGN,
		CommunityCategoryCode.SOPTICLE_SERVER,
		CommunityCategoryCode.SOPTICLE_WEB,
		CommunityCategoryCode.SOPTICLE_IOS,
		CommunityCategoryCode.SOPTICLE_ANDROID,
		CommunityCategoryCode.SOPTICLE_ETC
	);

	private static final Map<CommunityPostListFilter, CommunityCategoryCode> PROMOTION_FILTER_CODE_MAP = Map.of(
		CommunityPostListFilter.EVENT, CommunityCategoryCode.PROMOTION_EVENT,
		CommunityPostListFilter.PROJECT, CommunityCategoryCode.PROMOTION_PROJECT,
		CommunityPostListFilter.RECRUIT, CommunityCategoryCode.PROMOTION_RECRUIT,
		CommunityPostListFilter.ETC, CommunityCategoryCode.PROMOTION_ETC
	);

	private static final Map<CommunityPostListFilter, CommunityCategoryCode> SOPTICLE_FILTER_CODE_MAP = Map.of(
		CommunityPostListFilter.PLAN, CommunityCategoryCode.SOPTICLE_PLAN,
		CommunityPostListFilter.DESIGN, CommunityCategoryCode.SOPTICLE_DESIGN,
		CommunityPostListFilter.SERVER, CommunityCategoryCode.SOPTICLE_SERVER,
		CommunityPostListFilter.WEB, CommunityCategoryCode.SOPTICLE_WEB,
		CommunityPostListFilter.IOS, CommunityCategoryCode.SOPTICLE_IOS,
		CommunityPostListFilter.ANDROID, CommunityCategoryCode.SOPTICLE_ANDROID,
		CommunityPostListFilter.ETC, CommunityCategoryCode.SOPTICLE_ETC
	);

	private static final Set<CommunityPostListFilter> PROMOTION_FILTERS = Set.of(
		CommunityPostListFilter.ALL,
		CommunityPostListFilter.EVENT,
		CommunityPostListFilter.PROJECT,
		CommunityPostListFilter.RECRUIT,
		CommunityPostListFilter.ETC
	);

	private static final Set<CommunityPostListFilter> SOPTICLE_FILTERS = Set.of(
		CommunityPostListFilter.ALL,
		CommunityPostListFilter.PLAN,
		CommunityPostListFilter.DESIGN,
		CommunityPostListFilter.SERVER,
		CommunityPostListFilter.WEB,
		CommunityPostListFilter.IOS,
		CommunityPostListFilter.ANDROID,
		CommunityPostListFilter.ETC
	);

	public List<CommunityCategoryCode> resolveCategoryCodes(
		CommunityPostListCategory category,
		CommunityPostListFilter filter
	) {
		return switch (category) {
			case FREE -> resolveFreeCodes(filter);
			case PROMOTION -> resolvePromotionCodes(normalizeFilter(filter));
			case SOPTICLE -> resolveSopticleCodes(normalizeFilter(filter));
		};
	}

	public boolean isSopticleCategoryCode(CommunityCategoryCode categoryCode) {
		return SOPTICLE_ALL_CODES.contains(categoryCode);
	}

	public List<CommunityPostListFilter> getAvailableFilters(CommunityPostListCategory category) {
		return switch (category) {
			case FREE -> List.of();
			case PROMOTION -> List.of(
				CommunityPostListFilter.ALL,
				CommunityPostListFilter.EVENT,
				CommunityPostListFilter.PROJECT,
				CommunityPostListFilter.RECRUIT,
				CommunityPostListFilter.ETC
			);
			case SOPTICLE -> List.of(
				CommunityPostListFilter.ALL,
				CommunityPostListFilter.PLAN,
				CommunityPostListFilter.DESIGN,
				CommunityPostListFilter.SERVER,
				CommunityPostListFilter.WEB,
				CommunityPostListFilter.IOS,
				CommunityPostListFilter.ANDROID,
				CommunityPostListFilter.ETC
			);
		};
	}

	public CommunityPostTag resolvePreviewTag(Category category) {
		if (category == null || category.getCategoryGroup() == null) {
			return null;
		}

		if (category.getCategoryGroup() == CommunityCategoryGroup.FREE) {
			return CommunityPostTag.FREE;
		}

		if (category.getCategoryGroup() == CommunityCategoryGroup.SOPTICLE) {
			return CommunityPostTag.SOPTICLE;
		}

		if (category.getCategoryGroup() == CommunityCategoryGroup.PROMOTION) {
			return resolvePromotionPreviewTag(category.getCode());
		}

		return null;
	}

	private CommunityPostTag resolvePromotionPreviewTag(CommunityCategoryCode code) {
		if (code == null) {
			return CommunityPostTag.PROMOTION;
		}

		return switch (code) {
			case PROMOTION_EVENT -> CommunityPostTag.EVENT;
			case PROMOTION_PROJECT -> CommunityPostTag.PROJECT;
			case PROMOTION_RECRUIT -> CommunityPostTag.RECRUIT;
			case PROMOTION, PROMOTION_ETC -> CommunityPostTag.PROMOTION;
			default -> CommunityPostTag.PROMOTION;
		};
	}

	private List<CommunityCategoryCode> resolveFreeCodes(CommunityPostListFilter filter) {
		if (filter != null) {
			throw new BadRequestException("자유 카테고리는 filter 값을 받을 수 없습니다.");
		}

		return List.of(CommunityCategoryCode.FREE);
	}

	private List<CommunityCategoryCode> resolvePromotionCodes(CommunityPostListFilter filter) {
		if (!PROMOTION_FILTERS.contains(filter)) {
			throw new BadRequestException("홍보 카테고리에서 사용할 수 없는 filter 값입니다.");
		}

		if (filter == CommunityPostListFilter.ALL) {
			return PROMOTION_ALL_CODES;
		}

		return List.of(PROMOTION_FILTER_CODE_MAP.get(filter));
	}

	private List<CommunityCategoryCode> resolveSopticleCodes(CommunityPostListFilter filter) {
		if (!SOPTICLE_FILTERS.contains(filter)) {
			throw new BadRequestException("솝티클 카테고리에서 사용할 수 없는 filter 값입니다.");
		}

		if (filter == CommunityPostListFilter.ALL) {
			return SOPTICLE_ALL_CODES;
		}

		return List.of(SOPTICLE_FILTER_CODE_MAP.get(filter));
	}

	private CommunityPostListFilter normalizeFilter(CommunityPostListFilter filter) {
		return filter == null ? CommunityPostListFilter.ALL : filter;
	}
}