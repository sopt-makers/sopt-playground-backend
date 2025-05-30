package org.sopt.makers.internal.member.dto.request;

import javax.validation.constraints.NotNull;

public record MemberBlindRequest(

        @NotNull
        Boolean blind
) {
}
