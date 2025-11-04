package org.sopt.makers.internal.popup.service;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.popup.domain.Popup;
import org.sopt.makers.internal.popup.dto.request.PopupRequest;
import org.sopt.makers.internal.popup.dto.response.PopupResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PopupService {

    private final PopupRetriever popupRetriever;
    private final PopupModifier popupModifier;

    @Transactional
    public PopupResponse createPopup(PopupRequest request) {
        
        Popup savedPopup = popupModifier.createPopup(
                LocalDate.parse(request.startDate()),
                LocalDate.parse(request.endDate()),
                request.pcImageUrl(),
                request.mobileImageUrl(),
                request.linkUrl(),
                request.openInNewTab(),
                request.showOnlyToRecentGeneration()
        );

        return PopupResponse.from(savedPopup);
    }

    @Transactional(readOnly = true)
    public List<PopupResponse> getAllPopups() {
        return popupRetriever.findAllPopups().stream()
                .map(PopupResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PopupResponse getPopupById(Long id) {
        Popup popup = popupRetriever.findPopupById(id);
        return PopupResponse.from(popup);
    }

    @Transactional
    public PopupResponse updatePopup(Long id, PopupRequest request) {
        Popup popup = popupRetriever.findPopupById(id);
		
        popupModifier.updatePopup(
                popup,
                LocalDate.parse(request.startDate()),
                LocalDate.parse(request.endDate()),
                request.pcImageUrl(),
                request.mobileImageUrl(),
                request.linkUrl(),
                request.openInNewTab(),
                request.showOnlyToRecentGeneration()
        );

        return PopupResponse.from(popup);
    }

    @Transactional
    public void deletePopup(Long id) {
        Popup popup = popupRetriever.findPopupById(id);
        popupModifier.deletePopup(popup);
    }
}
