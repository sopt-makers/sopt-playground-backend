package org.sopt.makers.internal.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record RegistrationTokenBySmsRequest(@Schema(required = true) String sixNumberCode) {
}
