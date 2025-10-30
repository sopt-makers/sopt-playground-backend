package org.sopt.makers.internal.popup.service;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.sopt.makers.internal.popup.domain.Popup;
import org.sopt.makers.internal.popup.repository.PopupRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PopupRetriever {

    private final PopupRepository popupRepository;

    public Popup findPopupById(Long id) {
        return popupRepository.findById(id)
                .orElseThrow(() -> new NotFoundDBEntityException("존재하지 않는 팝업입니다. id: [" + id + "]"));
    }

    public List<Popup> findAllPopups() {
        return popupRepository.findAll();
    }
}