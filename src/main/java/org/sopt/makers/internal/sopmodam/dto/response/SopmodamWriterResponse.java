package org.sopt.makers.internal.sopmodam.dto.response;

import java.util.Comparator;

import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.external.platform.SoptActivity;

public record SopmodamWriterResponse(
    Long memberId,
    String name,
    String profileImage,
    Integer generation,
    String part
) {
    public static SopmodamWriterResponse from(InternalUserDetails user) {
        SoptActivity latest = user.soptActivities().stream()
            .max(Comparator.comparingInt(SoptActivity::normalizedGeneration))
            .orElse(null);
        return new SopmodamWriterResponse(
            user.userId(),
            user.name(),
            user.profileImage(),
            latest != null ? latest.generation() : user.lastGeneration(),
            latest != null ? latest.part() : null
        );
    }
}
