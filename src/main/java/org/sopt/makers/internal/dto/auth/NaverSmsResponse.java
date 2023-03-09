package org.sopt.makers.internal.dto.auth;

import java.time.LocalDateTime;

public record NaverSmsResponse(
        String requestId,
        LocalDateTime requestTime,
        String statusCode,
        String statusName
) {
}
