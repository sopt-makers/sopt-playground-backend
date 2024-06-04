package org.sopt.makers.internal.dto.auth;

public record GabiaSMSResponse(
        String code,
        String message,
        GabiaSMSResponseData data
) {
}
