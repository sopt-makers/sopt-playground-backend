package org.sopt.makers.internal.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PaginationUtil {

	private static final int DEFAULT_LIMIT = 20;
	private static final int MAX_LIMIT = 100;

	public static int validateAndGetLimit(Integer limit) {
		if (limit == null || limit <= 0) {
			return DEFAULT_LIMIT;
		}
		return Math.min(limit, MAX_LIMIT);
	}

	public static int validateAndGetLimit(Integer limit, int defaultLimit, int maxLimit) {
		if (limit == null || limit <= 0) {
			return defaultLimit;
		}
		return Math.min(limit, maxLimit);
	}
}