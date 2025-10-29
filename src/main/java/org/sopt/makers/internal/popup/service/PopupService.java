package org.sopt.makers.internal.popup.service;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.popup.domain.Popup;
import org.sopt.makers.internal.popup.dto.request.PopupRequest;
import org.sopt.makers.internal.popup.dto.response.PopupResponse;
import org.sopt.makers.internal.popup.repository.PopupRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PopupService {

    private final PopupRepository popupRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Transactional
    public PopupResponse createPopup(PopupRequest request) {
        LocalDate startDate = parseDate(request.startDate());
        LocalDate endDate = parseDate(request.endDate());

        validateDateRange(startDate, endDate);

        Popup popup = Popup.of(
                startDate,
                endDate,
                request.pcImageUrl(),
                request.mobileImageUrl(),
                request.linkUrl(),
                request.openInNewTab(),
                request.showOnlyToRecentGeneration()
        );

        Popup savedPopup = popupRepository.save(popup);
        return PopupResponse.from(savedPopup);
    }

    @Transactional(readOnly = true)
    public List<PopupResponse> getAllPopups() {
        return popupRepository.findAll().stream()
                .map(PopupResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PopupResponse getPopupById(Long id) {
        Popup popup = findPopupById(id);
        return PopupResponse.from(popup);
    }

    @Transactional
    public PopupResponse updatePopup(Long id, PopupRequest request) {
        Popup popup = findPopupById(id);

        LocalDate startDate = parseDate(request.startDate());
        LocalDate endDate = parseDate(request.endDate());

        validateDateRange(startDate, endDate);

        popup.update(
                startDate,
                endDate,
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
        Popup popup = findPopupById(id);
        popupRepository.delete(popup);
    }

    private Popup findPopupById(Long id) {
        return popupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "팝업을 찾을 수 없습니다."));
    }

    private LocalDate parseDate(String dateString) {
        try {
            return LocalDate.parse(dateString, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "날짜 형식이 올바르지 않습니다. (YYYY-MM-DD)");
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "시작 날짜는 종료 날짜보다 이전이어야 합니다.");
        }
    }
}
