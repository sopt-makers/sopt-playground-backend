package org.sopt.makers.internal.external.naver;

import java.time.LocalDateTime;

public record NaverSmsResponse(
        String requestId,
        LocalDateTime requestTime,
        String statusCode,
        String statusName
) {
}
