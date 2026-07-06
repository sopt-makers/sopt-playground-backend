package org.sopt.makers.internal.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record ProjectSaveRequest(
        @Schema(required = true)
        String name,
        @Schema(required = true)
        Long writerId,
        @Schema(required = true)
        Integer generation,
        @Schema(required = true)
        String category,
        @Schema(required = true)
        LocalDate startAt,
        LocalDate endAt,
        @Schema(required = true)
        List<String> serviceType,
        Boolean isAvailable,
        Boolean isFounding,
        @Schema(required = true)
        @NotBlank(message = "summary는 필수입니다.")
        @Size(max = 30, message = "summary는 30자 이하여야 합니다.")
        String summary,
        @Schema(required = true)
        @NotBlank(message = "detail은 필수입니다.")
        @Size(max = 3000, message = "detail은 3000자 이하여야 합니다.")
        String detail,
        @Schema(required = true)
        String logoImage,
        @Schema(required = true)
        String thumbnailImage,
        List<String> images,
        List<ProjectMemberSaveRequest> members,
        List<ProjectLinkSaveRequest> links
) {
    public record ProjectMemberSaveRequest(
            Long memberId,
            String memberRole,
            String memberDescription,
            Boolean isTeamMember
    ){}

    public record ProjectLinkSaveRequest(
            String linkTitle,
            String linkUrl
    ){}
}
