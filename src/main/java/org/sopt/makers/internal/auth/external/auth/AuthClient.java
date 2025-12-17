package org.sopt.makers.internal.auth.external.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.makers.internal.auth.external.code.ClientFailure;
import org.sopt.makers.internal.auth.external.exception.ClientException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthClient {

    private final RestTemplate authRestTemplate;
    private final AuthClientProperty authProperty;

    public String getJwk() {
        try {
            ResponseEntity<String> response = authRestTemplate.getForEntity(
                    authProperty.endpoints().jwk(),
                    String.class
            );
            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            log.error("Failed to receive response from Auth server: {}", ex.getResponseBodyAsString(), ex);
            throw new ClientException(ClientFailure.RESPONSE_ERROR);
        } catch (ResourceAccessException e) {
            log.error("Unexpected exception occurred during Auth server communication: {}", e.getMessage(), e);
            throw new ClientException(ClientFailure.COMMUNICATION_ERROR);
        } catch (RuntimeException e) {
            log.error("Unexpected exception occurred during Auth server communication: {}", e.getMessage(), e);
            throw new ClientException(ClientFailure.COMMUNICATION_ERROR);
        }
    }
}