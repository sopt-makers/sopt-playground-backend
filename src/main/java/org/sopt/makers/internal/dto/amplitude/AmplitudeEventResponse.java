package org.sopt.makers.internal.dto.amplitude;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AmplitudeEventResponse(
	List<EventDto> events,
	Object userData,
	Object metadata
) {
	public record EventDto(
		@JsonProperty("event_type")
		String eventType,
		@JsonProperty("event_id")
		Long eventId,
		@JsonProperty("client_event_time")
		String clientEventTime,
		@JsonProperty("event_time")
		String eventTime
	) {}
}
