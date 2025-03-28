package org.sopt.makers.internal.resolution.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.makers.internal.common.GoogleSheetsService;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserResolutionServiceUtil {

    private final GoogleSheetsService googleSheetsService;

    public void safeWriteToSheets(Long writerId, List<List<Object>> rowData) {
        try {
            googleSheetsService.writeSheetData(rowData);
        } catch (Exception e) {
            log.error("❌ 구글 스프레드 시트 연동 타임캡슐 작성 실패 - writerId={}, 이유: {}", writerId, e.getMessage(), e);
        }
    }
}
