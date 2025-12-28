package org.sopt.makers.internal.popup.service;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.exception.NotFoundException;
import org.sopt.makers.internal.popup.domain.Popup;
import org.sopt.makers.internal.popup.repository.PopupRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PopupRetriever {

    private final PopupRepository popupRepository;

    public Popup findPopupById(Long id) {
        return popupRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 팝업입니다. id: [" + id + "]"));
    }

    public List<Popup> findAllPopups() {
        return popupRepository.findAll();
    }

    public Popup findCurrentPopup() {
        return popupRepository.findFirstCurrentPopup(LocalDate.now()).orElse(null);
    }
}
