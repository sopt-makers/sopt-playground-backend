package org.sopt.makers.internal.external.apple;

public record AppleKeyVo (
        String kty,
        String kid,
        String use,
        String alg,
        String n,
        String e
) {}