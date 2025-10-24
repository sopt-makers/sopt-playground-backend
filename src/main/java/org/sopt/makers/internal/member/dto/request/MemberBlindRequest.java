package org.sopt.makers.internal.member.dto.request;

import jakarta.validation.constraints.NotNull;

public record MemberBlindRequest(

        @NotNull
        Boolean blind
) {
}
