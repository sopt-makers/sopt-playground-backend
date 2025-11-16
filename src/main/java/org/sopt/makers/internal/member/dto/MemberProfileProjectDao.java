package org.sopt.makers.internal.member.dto;

import org.springframework.aot.hint.annotation.Reflective;

import java.util.List;

@Reflective
public record MemberProfileProjectDao(
        Long id,
        Long writerId,
        String name,
        String summary,
        Integer generation,
        String category,
        String logoImage,
        String thumbnailImage,
        List<String> serviceType
) {
}
