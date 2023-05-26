package org.sopt.makers.internal.dto;

import java.util.List;

public record SopticleVo(
        Long id,
        String link,
        List<SopticleUserVo> writers
) {
    public record SopticleUserVo(
            Long id,
            String name,
            String part,
            Integer generation
    ) {}
}
