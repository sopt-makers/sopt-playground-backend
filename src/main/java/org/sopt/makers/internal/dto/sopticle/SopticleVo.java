package org.sopt.makers.internal.dto.sopticle;

import java.util.List;

public record SopticleVo(
        Long id,
        String link,
        List<SopticleUserVo> authors
) {
    public record SopticleUserVo(
            Long id,
            String name,
            String profileImage,
            String part,
            Integer generation
    ) {}
}
