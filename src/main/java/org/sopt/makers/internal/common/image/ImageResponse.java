package org.sopt.makers.internal.common.image;

public record ImageResponse(
        String signedUrl,
        String filename
) {
}
