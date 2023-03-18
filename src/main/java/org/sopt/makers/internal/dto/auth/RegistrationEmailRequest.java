package org.sopt.makers.internal.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

public record RegistrationEmailRequest(@Schema(required = true) String email) {
}
