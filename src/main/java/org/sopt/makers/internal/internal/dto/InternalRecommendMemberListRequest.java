package org.sopt.makers.internal.internal.dto;


import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Objects;

public record InternalRecommendMemberListRequest(

        @Schema(required = true, description = "세대 필터", example = "[32, 33, 34, 35, 36]")
        @NotEmpty
        List<Integer> generations,

		@Schema(description = "추천 조건 필터", example = "[{\"key\": \"UNIVERSITY\", \"value\": \"하버드\"}, {\"key\": \"MBTI\", \"value\": \"ENTJ\"}]")
        List<SearchContentResponse> filters

) {
    public record SearchContentResponse(
            @Schema(description = "필터 키", example = "MBTI")
            String key,

			@Schema(description = "필터 값", example = "ENTJ")
            String value
    ) {
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