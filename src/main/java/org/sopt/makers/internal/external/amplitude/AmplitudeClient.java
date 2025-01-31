package org.sopt.makers.internal.external.amplitude;

import org.sopt.makers.internal.dto.amplitude.AmplitudeEventResponse;
import org.sopt.makers.internal.dto.amplitude.AmplitudeUserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "amplitudeClient", url = "https://amplitude.com/api/2")
public interface AmplitudeClient {
	@GetMapping("/usersearch")
	AmplitudeUserResponse getAmplitudeUserId(
		@RequestParam("user") Long userId,
		@RequestHeader("Authorization") String authorization
	);

	@GetMapping(value = "/useractivity")
	AmplitudeEventResponse getUserProperty(
		@RequestParam("user") Long amplitudeUserId,
		@RequestHeader("Authorization") String authorization
	);
}
