package org.sopt.makers.internal.external.amplitude;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.sopt.makers.internal.exception.BusinessLogicException;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EventData {
	// TOTAL_DURATION_TIME(),
	// TOTAL_VISIT_COUNT(),
	MEMBER_TAB_VISIT_COUNT("Click-memberCard"),
	PROJECT_TAB_VISIT_COUNT("Click-projectCard"),
	COFFEE_CHAT_TAB_VISIT_COUNT("Click-coffeechatCard")
	// CREW_TAB_VISIT_COUNT(),
	// MY_PROFILE_VIEW_COUNT(),
	;

	private final String property;

	public static final List<String> REPORT_EVENT_PROPERTY_LIST = Stream.of(
		MEMBER_TAB_VISIT_COUNT, PROJECT_TAB_VISIT_COUNT, COFFEE_CHAT_TAB_VISIT_COUNT
	).map(EventData::getProperty).toList();

	private static final Map<String, EventData> EVENT_DATA_MAP = new HashMap<>();

	static {
		for (EventData data : values()) {
			EVENT_DATA_MAP.put(data.property, data);
		}
	}

	public static EventData of(String property) {
		EventData result = EVENT_DATA_MAP.get(property.toLowerCase());
		if (result == null) {
			throw new BusinessLogicException("Unknown property: " + property);
		}
		return result;
	}
}
