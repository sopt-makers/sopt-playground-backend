package org.sopt.makers.internal.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record CodeRequest(@Schema(required = true) String accessToken) {
}
