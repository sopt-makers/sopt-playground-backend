package org.sopt.makers.internal.popup.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.popup.auth.AdminKeyValidator;
import org.sopt.makers.internal.popup.dto.request.PopupRequest;
import org.sopt.makers.internal.popup.dto.response.PopupResponse;
import org.sopt.makers.internal.popup.service.PopupService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/popups")
@Tag(name = "팝업 관리 API", description = "웹 팝업 CRUD API (admin-key 헤더 필수)")
public class PopupController {

    private final PopupService popupService;
    private final AdminKeyValidator adminKeyValidator;

    @Operation(summary = "팝업 생성 API", description = "새로운 팝업을 생성합니다.")
    @PostMapping
    public ResponseEntity<PopupResponse> createPopup(
            @RequestHeader("admin-key") String adminKey,
            @Valid @RequestBody PopupRequest request
    ) {
        adminKeyValidator.validate(adminKey);
        PopupResponse response = popupService.createPopup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "전체 팝업 조회 API", description = "모든 팝업을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<PopupResponse>> getAllPopups(
            @RequestHeader("admin-key") String adminKey
    ) {
        adminKeyValidator.validate(adminKey);
        List<PopupResponse> responses = popupService.getAllPopups();
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @Operation(summary = "팝업 상세 조회 API", description = "특정 팝업을 ID로 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<PopupResponse> getPopupById(
            @RequestHeader("admin-key") String adminKey,
            @PathVariable Long id
    ) {
        adminKeyValidator.validate(adminKey);
        PopupResponse response = popupService.getPopupById(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "팝업 수정 API", description = "기존 팝업 정보를 수정합니다.")
    @PutMapping("/{id}")
    public ResponseEntity<PopupResponse> updatePopup(
            @RequestHeader("admin-key") String adminKey,
            @PathVariable Long id,
            @Valid @RequestBody PopupRequest request
    ) {
        adminKeyValidator.validate(adminKey);
        PopupResponse response = popupService.updatePopup(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "팝업 삭제 API", description = "팝업을 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePopup(
            @RequestHeader("admin-key") String adminKey,
            @PathVariable Long id
    ) {
        adminKeyValidator.validate(adminKey);
        popupService.deletePopup(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(summary = "Admin Key 검증 API", description = "Admin Key가 유효한지 검증합니다.")
    @PostMapping("/validate-admin-key")
    public ResponseEntity<Void> validateAdminKey(
            @RequestHeader("admin-key") String adminKey
    ) {
        adminKeyValidator.validate(adminKey);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(summary = "현재 진행중인 팝업 조회 API", description = "현재 운영 기간에 포함되는 팝업 중 시작 날짜가 가장 빠른 팝업을 조회합니다. 없으면 null을 반환합니다.")
    @SecurityRequirement(name = "Authorization")
    @GetMapping("/current")
    public ResponseEntity<PopupResponse> getCurrentPopup() {
        PopupResponse response = popupService.getCurrentPopup();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
