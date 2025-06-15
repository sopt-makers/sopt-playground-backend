package org.sopt.makers.internal.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record RegisterTokenResponse(@Schema(required = true) String registerToken) {
}
