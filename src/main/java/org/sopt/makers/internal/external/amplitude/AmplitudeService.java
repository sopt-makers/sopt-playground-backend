package org.sopt.makers.internal.external.amplitude;

import static org.sopt.makers.internal.external.amplitude.EventData.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.sopt.makers.internal.dto.amplitude.AmplitudeEventResponse;
import org.sopt.makers.internal.dto.amplitude.AmplitudeUserResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AmplitudeService {

	private final AmplitudeClient amplitudeClient;

	@Value("${amplitude.api-key}")
	private String username;

	@Value("${amplitude.secret-key}")
	private String password;

	public Map<String, Long> getUserEventData(Long userId) {
		String authHeader = "Basic " + encodeBasicAuth(username, password);
		AmplitudeUserResponse response = amplitudeClient.getAmplitudeUserId(userId, authHeader);
		Long amplitudeUserId = response.matches().get(0).amplitudeId();
		List<AmplitudeEventResponse.EventDto> events = amplitudeClient.getUserProperty(amplitudeUserId, authHeader).events();

		return countEventTypes(events);
	}

	private String encodeBasicAuth(String username, String password) {
		String auth = username + ":" + password;
		return Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
	}

	private Map<String, Long> countEventTypes(List<AmplitudeEventResponse.EventDto> events) {
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		return events.stream()
			.filter(event -> REPORT_EVENT_PROPERTY_LIST.contains(event.eventType()) &&
				LocalDate.parse(event.eventTime().substring(0, 10), dateFormatter).getYear() == 2024)
			.collect(Collectors.groupingBy(AmplitudeEventResponse.EventDto::eventType, Collectors.counting()));
	}
}
