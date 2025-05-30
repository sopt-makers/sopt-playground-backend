package org.sopt.makers.internal.member.dto;

public record ActivityVo(
        Long id,
        Integer generation,
        String team,
        String part,
        boolean isProject
) {
}
