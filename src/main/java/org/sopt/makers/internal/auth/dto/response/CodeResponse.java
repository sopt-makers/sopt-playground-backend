package org.sopt.makers.internal.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record CodeResponse(@Schema(required = true) String code) {
}
