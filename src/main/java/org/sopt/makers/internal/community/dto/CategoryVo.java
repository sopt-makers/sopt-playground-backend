package org.sopt.makers.internal.community.dto;

import org.sopt.makers.internal.community.domain.enums.CommunityCategoryCode;
import org.sopt.makers.internal.community.domain.enums.CommunityCategoryGroup;
import org.springframework.aot.hint.annotation.Reflective;

@Reflective
public record CategoryVo(
	CommunityCategoryGroup categoryGroup,
	CommunityCategoryCode code,
	String name,
	CommunityCategoryCode parentCode,
	String parentCategoryName
) {
}