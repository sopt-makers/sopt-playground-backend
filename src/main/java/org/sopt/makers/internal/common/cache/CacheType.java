package org.sopt.makers.internal.common.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CacheType {

	TYPE_COMMON_SOPT_REPORT_STAS(CacheConstant.TYPE_COMMON_SOPT_REPORT_STATS, 10L, 3600L),
	MY_SOPT_REPORT_MY_PLAYGROUND_DATA(CacheConstant.TYPE_MY_SOPT_REPORT_STATS, 1000L, 604800L)  // 일주일

	;

	private final String cacheName;
	private final Long maximumSize;
	private final Long expireAfterWriteOfSeconds;
}
