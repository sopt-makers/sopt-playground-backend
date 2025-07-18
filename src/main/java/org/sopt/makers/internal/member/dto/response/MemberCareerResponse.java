package org.sopt.makers.internal.member.dto.response;

public record MemberCareerResponse(
        Long id,
        String companyName,
        String title,
        Boolean isCurrent
){}
