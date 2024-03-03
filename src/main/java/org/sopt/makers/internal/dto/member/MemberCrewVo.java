package org.sopt.makers.internal.dto.member;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record MemberCrewVo(

        @NotNull
        Long id,

        @NotNull
        Boolean isMeetingLeader,

        @NotBlank
        String title,

        String imageUrl,

        String category,

        @NotNull
        Boolean isActiveMeeting,

        @NotNull
        LocalDateTime mstartDate,

        @NotNull
        LocalDateTime mendDate
) {
}
