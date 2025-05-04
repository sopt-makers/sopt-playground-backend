package org.sopt.makers.internal.external.message.gabia.dto;

public record GabiaSMSResponse(
        String code,
        String message,
        GabiaSMSResponseData data
) {
}
