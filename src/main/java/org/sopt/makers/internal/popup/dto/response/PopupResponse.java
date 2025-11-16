package org.sopt.makers.internal.popup.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.sopt.makers.internal.popup.domain.Popup;

public record PopupResponse(
        @Schema(description = "팝업 ID", example = "1")
        Long id,

        @Schema(description = "팝업 시작 날짜", example = "2024-01-01")
        String startDate,

        @Schema(description = "팝업 종료 날짜", example = "2024-01-31")
        String endDate,

        @Schema(description = "PC 이미지 URL", example = "https://example.com/popup-pc.jpg")
        String pcImageUrl,

        @Schema(description = "모바일 이미지 URL", example = "https://example.com/popup-mobile.jpg")
        String mobileImageUrl,

        @Schema(description = "팝업 클릭 시 이동할 링크", example = "https://sopt.org")
        String linkUrl,

        @Schema(description = "새 탭에서 열지 여부", example = "true")
        Boolean openInNewTab,

        @Schema(description = "최근 기수에게만 보여주는 여부", example = "false")
        Boolean showOnlyToRecentGeneration
) {
    public static PopupResponse from(Popup popup) {
        return new PopupResponse(
                popup.getId(),
                popup.getStartDate().toString(),
                popup.getEndDate().toString(),
                popup.getPcImageUrl(),
                popup.getMobileImageUrl(),
                popup.getLinkUrl(),
                popup.getOpenInNewTab(),
                popup.getShowOnlyToRecentGeneration()
        );
    }
}
