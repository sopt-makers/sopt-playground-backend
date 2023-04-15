package org.sopt.makers.internal.dto.internal;

public record InternalAuthResponse (
        String accessToken,
        String errorCode
) {
}
