package org.sopt.makers.internal.popup.auth;

import org.sopt.makers.internal.exception.ForbiddenException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class AdminKeyValidator {

    @Value("${admin.key}")
    private String adminKey;

    public void validate(String providedAdminKey) {
        if (!Objects.equals(adminKey, providedAdminKey)) {
            throw new ForbiddenException("잘못된 Admin-Key입니다.");
        }
    }
}
