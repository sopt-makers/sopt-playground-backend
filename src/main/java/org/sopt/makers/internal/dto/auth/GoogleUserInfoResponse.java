package org.sopt.makers.internal.dto.auth;

public record GoogleUserInfoResponse(
        boolean verified,
        String userId
) {}
