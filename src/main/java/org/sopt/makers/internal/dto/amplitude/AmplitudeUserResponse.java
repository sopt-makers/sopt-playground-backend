package org.sopt.makers.internal.dto.amplitude;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AmplitudeUserResponse(
    List<Match> matches
) {
    public record Match (
        @JsonProperty("user_id")
        Long userId,
        @JsonProperty("amplitude_id")
        Long amplitudeId,
        String platform,
        String country
    ) {}
}
