package org.sopt.makers.internal.external.apple;

import org.springframework.aot.hint.annotation.Reflective;

@Reflective
public record AppleKeyVo (
        String kty,
        String kid,
        String use,
        String alg,
        String n,
        String e
) {}