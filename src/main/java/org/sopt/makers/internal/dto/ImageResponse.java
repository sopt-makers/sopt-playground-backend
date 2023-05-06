package org.sopt.makers.internal.dto;

public record ImageResponse(
        String signedUrl,
        String filename
) {
}
