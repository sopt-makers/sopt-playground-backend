package org.sopt.makers.internal.internal.auth;

import java.util.Objects;

import org.sopt.makers.internal.auth.AuthConfig;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ApiKeyValidator {

	private final AuthConfig authConfig;

	public ApiKeyValidator(AuthConfig authConfig) {
		this.authConfig = authConfig;
	}

	public void validate(String providedApiKey) {
		if (!Objects.equals(authConfig.getInternalPlatformApiKey(), providedApiKey)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "잘못된 api key 입니다.");
		}
	}
}
