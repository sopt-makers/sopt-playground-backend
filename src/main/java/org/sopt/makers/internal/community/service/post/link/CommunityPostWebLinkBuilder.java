package org.sopt.makers.internal.community.service.post.link;

import java.util.Objects;
import org.sopt.makers.internal.community.domain.CommunityPost;
import org.sopt.makers.internal.community.domain.category.Category;
import org.sopt.makers.internal.community.domain.enums.CommunityCategoryCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CommunityPostWebLinkBuilder {

	private static final String PROD_PROFILE = "prod";
	private static final String PROD_BASE_URL = "https://playground.sopt.org";
	private static final String DEV_BASE_URL = "https://sopt-internal-dev.pages.dev";

	private final String activeProfile;

	public CommunityPostWebLinkBuilder(
		@Value("${spring.profiles.active}") String activeProfile
	) {
		this.activeProfile = activeProfile;
	}

	public String build(CommunityPost post) {
		String baseUrl = resolveBaseUrl();
		Category category = post.getCategory();

		if (category == null || category.getCode() == null) {
			return baseUrl + "/feed?feed=" + post.getId();
		}

		CommunityCategoryCode rootCategoryCode = resolveRootCategoryCode(category);

		if (rootCategoryCode == CommunityCategoryCode.MEETING) {
			return baseUrl + "/group/post?id=" + post.getId();
		}

		StringBuilder webLink = new StringBuilder(baseUrl)
			.append("/feed?category=")
			.append(rootCategoryCode.name())
			.append("&feed=")
			.append(post.getId());

		String subcategory = resolveSubcategory(category);
		if (subcategory != null) {
			webLink.append("&subcategory=").append(subcategory);
		}

		return webLink.toString();
	}

	private String resolveBaseUrl() {
		return Objects.equals(activeProfile, PROD_PROFILE)
			? PROD_BASE_URL
			: DEV_BASE_URL;
	}

	private CommunityCategoryCode resolveRootCategoryCode(Category category) {
		return category.getParent() == null
			? category.getCode()
			: category.getParent().getCode();
	}

	private String resolveSubcategory(Category category) {
		if (category.getParent() == null) {
			return null;
		}

		String rootPrefix = category.getParent().getCode().name() + "_";
		String categoryCode = category.getCode().name();

		return categoryCode.startsWith(rootPrefix)
			? categoryCode.substring(rootPrefix.length())
			: categoryCode;
	}
}