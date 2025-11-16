package org.sopt.makers.internal.popup.service;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.common.image.S3ImageService;
import org.sopt.makers.internal.popup.domain.Popup;
import org.sopt.makers.internal.popup.repository.PopupRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class PopupModifier {

    private final PopupRepository popupRepository;
    private final S3ImageService s3ImageService;

    // CREATE
    public Popup createPopup(LocalDate startDate, LocalDate endDate, String pcImageUrl,
                             String mobileImageUrl, String linkUrl, Boolean openInNewTab,
                             Boolean showOnlyToRecentGeneration) {
        Popup popup = Popup.builder()
                .startDate(startDate)
                .endDate(endDate)
                .pcImageUrl(pcImageUrl)
                .mobileImageUrl(mobileImageUrl)
                .linkUrl(linkUrl)
                .openInNewTab(openInNewTab)
                .showOnlyToRecentGeneration(showOnlyToRecentGeneration)
                .build();

        return popupRepository.save(popup);
    }

    // UPDATE
    public void updatePopup(Popup popup, LocalDate startDate, LocalDate endDate,
                            String pcImageUrl, String mobileImageUrl, String linkUrl,
                            Boolean openInNewTab, Boolean showOnlyToRecentGeneration) {
        // 이미지가 변경된 경우 기존 이미지 삭제
        String oldPcImageUrl = popup.getPcImageUrl();
        String oldMobileImageUrl = popup.getMobileImageUrl();

        popup.update(
                startDate,
                endDate,
                pcImageUrl,
                mobileImageUrl,
                linkUrl,
                openInNewTab,
                showOnlyToRecentGeneration
        );

        // 기존 이미지와 다르면 S3에서 삭제
        if (!oldPcImageUrl.equals(pcImageUrl)) {
            s3ImageService.deleteImage(oldPcImageUrl);
        }
        if (!oldMobileImageUrl.equals(mobileImageUrl)) {
            s3ImageService.deleteImage(oldMobileImageUrl);
        }
    }

    // DELETE
    public void deletePopup(Popup popup) {
        // S3에서 이미지 삭제
        s3ImageService.deleteImage(popup.getPcImageUrl());
        s3ImageService.deleteImage(popup.getMobileImageUrl());

        popupRepository.delete(popup);
    }
}