package org.sopt.makers.internal.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

public record RegisterTokenResponse(@Schema(required = true) String registerToken) {
}
