package org.sopt.makers.internal.dto.member;

import javax.validation.constraints.NotNull;

public record MemberBlindRequest(

        @NotNull
        Boolean blind
) {
}
