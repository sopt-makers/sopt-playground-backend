package org.sopt.makers.internal.dto.auth;

public record SmsCodeResponse(
        boolean success, String code, String message
) {}
