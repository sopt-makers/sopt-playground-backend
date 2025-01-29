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
		String eventTime,
		@JsonProperty("event_properties")
		EventPropertyDto eventProperties
	) {}

	public record EventPropertyDto(
		@JsonProperty("[Amplitude] Page URL")
		String pageUrl,
		@JsonProperty("[Amplitude] Page Path")
		String pagePath,
		@JsonProperty("[Amplitude] Page Title")
		String pageTitle,
		@JsonProperty("[Amplitude] Page Location")
		String pageLocation
	) {}
}
