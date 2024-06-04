package org.sopt.makers.internal.dto.auth;

public record GabiaSMSRequest(
        String phone,
        String callback,
        String message,
        String refkey
) {
}
