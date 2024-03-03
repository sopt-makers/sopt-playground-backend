package org.sopt.makers.internal.dto.member;

import org.sopt.makers.internal.dto.PaginationMeta;

import javax.validation.constraints.NotNull;
import java.util.List;

public record MemberCrewResponse(


        @NotNull
        List<MemberCrewVo> meetings,

        @NotNull
        PaginationMeta meta
) {
}
