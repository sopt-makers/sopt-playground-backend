package org.sopt.makers.internal.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record RegistrationEmailRequest(@Schema(required = true) String email) {
}
