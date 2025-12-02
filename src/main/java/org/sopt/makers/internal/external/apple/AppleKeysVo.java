package org.sopt.makers.internal.external.apple;

import org.springframework.aot.hint.annotation.Reflective;

import java.util.List;

@Reflective
public record AppleKeysVo(
        List<AppleKeyVo> keys
) {}
