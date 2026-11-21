package org.sopt.makers.internal.common.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CacheType {

	TYPE_COMMON_SOPT_REPORT_STAS(CacheConstant.TYPE_COMMON_SOPT_REPORT_STATS, 10L, 3600L),
	MY_SOPT_REPORT_MY_PLAYGROUND_DATA(CacheConstant.TYPE_MY_SOPT_REPORT_STATS, 1000L, 604800L),  // 일주일
	PLATFORM_USER_SEARCH(CacheConstant.PLATFORM_USER_SEARCH, 200L, 3600L)  // 1시간: 기수/파트별 멤버 목록은 자주 변하지 않음

	;

	private final String cacheName;
	private final Long maximumSize;
	private final Long expireAfterWriteOfSeconds;
}
