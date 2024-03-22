package org.sopt.makers.internal.dto.member;

import javax.validation.constraints.NotNull;

public record CheckActivityRequest(

        @NotNull
        Boolean isCheck
) {
}
