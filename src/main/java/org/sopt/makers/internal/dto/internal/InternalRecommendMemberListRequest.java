package org.sopt.makers.internal.dto.internal;

import java.util.List;
import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;

public record InternalRecommendMemberListRequest(

	@Schema(required = true)
	List<Integer> generations,

	List<SearchContentResponse> filters

) {
	public record SearchContentResponse(
		String key,
		String value
	){
	}

	public String getValueByKey(SearchContent key) {
		if (filters == null || key == null) {
			return null;
		}
		return filters.stream()
			.filter(content -> Objects.equals(SearchContent.of(content.key()), key))
			.map(SearchContentResponse::value)
			.findFirst()
			.orElse(null);
	}
}