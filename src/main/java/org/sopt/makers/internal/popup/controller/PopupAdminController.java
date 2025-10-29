package org.sopt.makers.internal.popup.controller;

import lombok.RequiredArgsConstructor;

import org.sopt.makers.internal.auth.AuthConfig;
import org.sopt.makers.internal.popup.dto.response.PopupResponse;
import org.sopt.makers.internal.popup.service.PopupService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class PopupAdminController {

    private final PopupService popupService;
    private final AuthConfig authConfig;

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
}
