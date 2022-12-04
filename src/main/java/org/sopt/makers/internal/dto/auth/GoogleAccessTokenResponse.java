package org.sopt.makers.internal.dto.auth;

public record GoogleAccessTokenResponse (
        boolean verified,
        String userId
) {}
