package org.sopt.makers.internal.dto.auth;

public record RegisterTokenBySmsResponse(
        boolean success, String code, String message,
        String registerToken
) {
}
