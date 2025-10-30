package org.sopt.makers.internal.popup.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PopupRequest(
        @Schema(required = true, description = "팝업 시작 날짜 (YYYY-MM-DD)", example = "2024-01-01")
        @NotBlank(message = "시작 날짜는 필수입니다.")
        String startDate,

        @Schema(required = true, description = "팝업 종료 날짜 (YYYY-MM-DD)", example = "2024-01-31")
        @NotBlank(message = "종료 날짜는 필수입니다.")
        String endDate,

        @Schema(required = true, description = "PC 이미지 URL", example = "https://example.com/popup-pc.jpg")
        @NotBlank(message = "PC 이미지 URL은 필수입니다.")
        String pcImageUrl,

        @Schema(required = true, description = "모바일 이미지 URL", example = "https://example.com/popup-mobile.jpg")
        @NotBlank(message = "모바일 이미지 URL은 필수입니다.")
        String mobileImageUrl,

        @Schema(description = "팝업 클릭 시 이동할 링크 (선택)", example = "https://sopt.org")
        String linkUrl,

        @Schema(description = "새 탭에서 열지 여부 (선택)", example = "true")
        Boolean openInNewTab,

        @Schema(description = "최근 기수에게만 보여주는 여부 (선택)", example = "false")
        Boolean showOnlyToRecentGeneration
) {
}