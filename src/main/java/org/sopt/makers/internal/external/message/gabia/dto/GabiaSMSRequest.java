package org.sopt.makers.internal.external.message.gabia.dto;

public record GabiaSMSRequest(
        String phone,
        String callback,
        String message,
        String refkey
) {
}
