package org.sopt.makers.internal.dto.member;

public record ActivityVo(
        Long id,
        Integer generation,
        String team,
        boolean isProject
) {
}
