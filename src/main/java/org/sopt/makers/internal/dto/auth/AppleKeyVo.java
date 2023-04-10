package org.sopt.makers.internal.dto.auth;

public record AppleKeyVo (
        String kty,
        String kid,
        String use,
        String alg,
        String n,
        String e
) {}