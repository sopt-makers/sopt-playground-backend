package org.sopt.makers.internal.member.domain.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum OrderByCondition {
	LATEST_REGISTERED(1),
	OLDEST_REGISTERED(2),
	LATEST_GENERATION(3),
	OLDEST_GENERATION(4);

	private final Integer orderByCondition;
	private static final Map<Integer, OrderByCondition> orderByConditionMap = new HashMap<>();
	OrderByCondition(Integer orderByCondition) {
		this.orderByCondition = orderByCondition;
	}

	static {
		for (OrderByCondition order : OrderByCondition.values()) {
			orderByConditionMap.put(
				order.getOrderByCondition(),
				order
			);
		}
	}

	public static OrderByCondition valueOf(Integer orderBy) {
		if (orderBy == null) return null;
		return orderByConditionMap.get(orderBy);
	}
}
