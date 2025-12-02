package org.sopt.makers.internal.popup.auth;

import org.sopt.makers.internal.exception.ForbiddenClientException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@Component
public class AdminKeyValidator {

    @Value("${admin.key}")
    private String adminKey;

    public void validate(String providedAdminKey) {
        if (!Objects.equals(adminKey, providedAdminKey)) {
            throw new ForbiddenClientException("잘못된 Admin-Key입니다.");
        }
    }
}
