package org.sopt.makers.internal.project.dto.response.allProject;

public record ProjectMemberResponse(
        Long memberId,
        String memberName,
        String memberProfileImage
){}