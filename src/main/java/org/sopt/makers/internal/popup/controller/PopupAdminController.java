package org.sopt.makers.internal.popup.controller;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.auth.AuthConfig;
import org.sopt.makers.internal.common.image.S3ImageService;
import org.sopt.makers.internal.popup.auth.AdminKeyValidator;
import org.sopt.makers.internal.popup.dto.response.PopupResponse;
import org.sopt.makers.internal.popup.service.PopupService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class PopupAdminController {

    private final PopupService popupService;
    private final AuthConfig authConfig;
    private final S3ImageService s3ImageService;
    private final AdminKeyValidator adminKeyValidator;

    @GetMapping
    public String adminLogin() {
        return "admin/login";
    }

    @GetMapping("/popups")
    public String listPopups(Model model) {
        List<PopupResponse> popups = popupService.getAllPopups();
        model.addAttribute("popups", popups);
        model.addAttribute("environment", authConfig.getActiveProfile());
        return "admin/popup-list";
    }

    @GetMapping("/popups/new")
    public String newPopupForm(Model model) {
        model.addAttribute("popup", new PopupResponse(null, "", "", "", "", "", null, null));
        model.addAttribute("isEdit", false);
        model.addAttribute("environment", authConfig.getActiveProfile());
        return "admin/popup-form";
    }

    @GetMapping("/popups/{id}/edit")
    public String editPopupForm(@PathVariable Long id, Model model) {
        PopupResponse popup = popupService.getPopupById(id);
        model.addAttribute("popup", popup);
        model.addAttribute("isEdit", true);
        model.addAttribute("environment", authConfig.getActiveProfile());
        return "admin/popup-form";
    }

    /**
     * 이미지 업로드 API (multipart/form-data)
     */
    @PostMapping("/popups/upload-image")
    @ResponseBody
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestHeader("admin-key") String adminKey,
            @RequestParam("file") MultipartFile file
    ) {
        adminKeyValidator.validate(adminKey);

        try {
            String imageUrl = s3ImageService.uploadImage(file, "popup");
            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", imageUrl);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "이미지 업로드 실패: " + e.getMessage()));
        }
    }
}
