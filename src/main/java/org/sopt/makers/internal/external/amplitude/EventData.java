package org.sopt.makers.internal.external.amplitude;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.sopt.makers.internal.exception.BusinessLogicException;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EventData {
	// TOTAL_DURATION_TIME(),
	TOTAL_VISIT_COUNT("[Amplitude] Start Session", ""),
	MEMBER_TAB_VISIT_COUNT("[Amplitude] Page Viewed", "/members"),
	PROJECT_TAB_VISIT_COUNT("[Amplitude] Page Viewed", "/projects"),
	COFFEE_CHAT_TAB_VISIT_COUNT("[Amplitude] Page Viewed", "/coffeechat"),
	// CREW_TAB_VISIT_COUNT(),
	MEMBER_PROFILE_CARD_VIEW_COUNT("Click-memberCard", ""),
	;

	final String property;
	final String pagePath;

	// Constant
	public static final Set<String> REPORT_EVENT_PROPERTY_SET = Stream.of(EventData.values())
		.map(EventData::getProperty).collect(Collectors.toSet());
	public static final Map<String, Long> DEFAULT_REPORT_EVENT_PROPERTY_MAP = Stream.of(EventData.values())
		.collect(Collectors.toMap(
			EventData::generateEventKey,
			v -> 0L
		));

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

	public static String generateEventKey(EventData event) {
		if (!Objects.equals(event.pagePath, "")) {
			return event.property + "|" + event.pagePath;
		}
		return event.property;
	}
}
