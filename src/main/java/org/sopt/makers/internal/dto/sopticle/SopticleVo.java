package org.sopt.makers.internal.dto.sopticle;

public record SopticleVo(
        String link,
        SopticleUserVo author
) {
    public record SopticleUserVo(
            Long id,
            String name,
            String profileImage,
            Integer generation,
            String part
    ) {}
}
