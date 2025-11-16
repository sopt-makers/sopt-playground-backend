package org.sopt.makers.internal.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.aot.hint.annotation.Reflective;

import java.time.LocalDateTime;

@Reflective
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
