package org.sopt.makers.internal.member.dto.request;

import jakarta.validation.constraints.NotNull;

public record CheckActivityRequest(

        @NotNull
        Boolean isCheck
) {
}
