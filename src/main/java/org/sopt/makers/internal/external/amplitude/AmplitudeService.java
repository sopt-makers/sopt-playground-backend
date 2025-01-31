package org.sopt.makers.internal.external.amplitude;

import static org.sopt.makers.internal.common.Constant.*;
import static org.sopt.makers.internal.external.amplitude.EventData.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.sopt.makers.internal.dto.amplitude.AmplitudeEventResponse;
import org.sopt.makers.internal.dto.amplitude.AmplitudeUserResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AmplitudeService {

	private final AmplitudeClient amplitudeClient;

	@Value("${amplitude.api-key}")
	private String username;

	@Value("${amplitude.secret-key}")
	private String password;

	@Value("${amplitude.crew-api-key}")
	private String crewUsername;

	@Value("${amplitude.crew-secret-key}")
	private String crewPassword;

	public Map<String, Long> getUserEventData(Long userId) {
		return fetchUserEventData(userId, username, password);
	}

	public Map<String, Long> getCrewUserEventData(Long userId) {
		return fetchUserEventData(userId, crewUsername, crewPassword);
	}

	public Map<String, Long> getAllUserEventData(Long userId) {
		Map<String, Long> userEvents = getUserEventData(userId);
		Map<String, Long> crewEvents = getCrewUserEventData(userId);

		return mergeEventCounts(userEvents, crewEvents);
	}

	private Map<String, Long> fetchUserEventData(Long userId, String apiKey, String secretKey) {
		try {
			String authHeader = "Basic " + encodeBasicAuth(apiKey, secretKey);
			AmplitudeUserResponse response = amplitudeClient.getAmplitudeUserId(userId, authHeader);
			Long amplitudeUserId = response.matches().get(0).amplitudeId();
			List<AmplitudeEventResponse.EventDto> events = amplitudeClient.getUserProperty(amplitudeUserId, authHeader)
				.events();

			return countEventTypes(events);
		} catch (FeignException ex) {
			return DEFAULT_REPORT_EVENT_PROPERTY_MAP;
		}
	}

	private String encodeBasicAuth(String username, String password) {
		String auth = username + ":" + password;
		return Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
	}

	private Map<String, Long> mergeEventCounts(Map<String, Long> map1, Map<String, Long> map2) {
		Map<String, Long> merged = new HashMap<>(map1);
		map2.forEach((key, value) -> merged.merge(key, value, Long::sum));
		return merged;
	}

	private Map<String, Long> countEventTypes(List<AmplitudeEventResponse.EventDto> events) {
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		Map<String, Long> eventCounts = events.stream()
			.filter(event -> REPORT_EVENT_PROPERTY_SET.contains(event.eventType()) &&
				LocalDate.parse(event.eventTime().substring(0, 10), dateFormatter).getYear() == REPORT_FILTER_YEAR)
			.collect(Collectors.groupingBy(this::generateEventKey, Collectors.counting()));

		return Stream.concat(eventCounts.entrySet().stream(), DEFAULT_REPORT_EVENT_PROPERTY_MAP.entrySet().stream())
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Math::max));
	}

	// 특정 Event Property에 대해 pageUrl을 포함하여 Key 생성
	private String generateEventKey(AmplitudeEventResponse.EventDto event) {
		Set<String> eventTypesWithPageUrl = Set.of("[Amplitude] Page Viewed");

		if (eventTypesWithPageUrl.contains(event.eventType()) && event.eventProperties() != null) {
			return event.eventType() + "|" + event.eventProperties().pagePath();
		}
		return event.eventType();
	}
}
