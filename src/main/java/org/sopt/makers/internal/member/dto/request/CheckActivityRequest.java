package org.sopt.makers.internal.member.dto.request;

import javax.validation.constraints.NotNull;

public record CheckActivityRequest(

        @NotNull
        Boolean isCheck
) {
}
