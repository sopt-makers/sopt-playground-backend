package org.sopt.makers.internal.project.dto.response.allProject;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

public record RandomProjectResponse(
        @Schema(required = true)
        Long id,
        @Schema(required = true)
        String name,
        @Schema(required = true)
        Integer generation,
        @Schema(description = "활동 정보 (앱잼, 솝커톤 등)", required = true)
        String category,
        @Schema(description = "플랫폼 정보 (iOS, Android, Web 등)", required = true)
        List<String> serviceType,
        @Schema(description = "활동 시작일")
        LocalDate startAt,
        @Schema(description = "활동 종료일")
        LocalDate endAt,
        @Schema(description = "서비스 운영 여부")
        Boolean isAvailable,
        @Schema(required = true)
        String logoImage,
        @Schema(required = true)
        String thumbnailImage
) { }