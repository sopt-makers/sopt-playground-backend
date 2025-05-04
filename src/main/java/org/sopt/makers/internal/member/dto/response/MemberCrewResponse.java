package org.sopt.makers.internal.member.dto.response;

import org.sopt.makers.internal.external.makers.PaginationMeta;
import org.sopt.makers.internal.member.dto.MemberCrewVo;

import javax.validation.constraints.NotNull;
import java.util.List;

public record MemberCrewResponse(


        @NotNull
        List<MemberCrewVo> meetings,

        @NotNull
        PaginationMeta meta
) {
}
