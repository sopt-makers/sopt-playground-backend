package org.sopt.makers.internal.internal.dto;

public record InternalAuthResponse (
        String accessToken,
        String errorCode
) {
}
