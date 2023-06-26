package org.sopt.makers.internal.dto.sopticle;

public record SopticleOfficialResponse(
        Integer statusCode,
        String message,
        String error
) {
}
