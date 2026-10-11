package org.sopt.makers.internal.sopmodam.controller;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.auth.AuthConfig;
import org.sopt.makers.internal.common.Constant;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

// /admin/** 은 인증 없이 열리므로 화면 틀만 내려준다. 데이터는 화면에서 admin-key 헤더로 어드민 API 를 호출해 채운다
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/sopmodam")
public class SopmodamAdminPageController {

    private static final String ACTIVE_MENU = "sopmodam";

    private final AuthConfig authConfig;

    @GetMapping("/rounds")
    public String roundList(Model model) {
        addCommonAttributes(model);
        return "admin/sopmodam/round-list";
    }

    @GetMapping("/rounds/new")
    public String roundNew(Model model) {
        addCommonAttributes(model);
        model.addAttribute("currentGeneration", Constant.CURRENT_GENERATION);
        return "admin/sopmodam/round-new";
    }

    @GetMapping("/rounds/{roundId}")
    public String roundDetail(@PathVariable("roundId") Long roundId, Model model) {
        addCommonAttributes(model);
        model.addAttribute("roundId", roundId);
        return "admin/sopmodam/round-detail";
    }

    @GetMapping("/rounds/{roundId}/answers")
    public String roundAnswers(@PathVariable("roundId") Long roundId, Model model) {
        addCommonAttributes(model);
        model.addAttribute("roundId", roundId);
        return "admin/sopmodam/round-answers";
    }

    private void addCommonAttributes(Model model) {
        model.addAttribute("environment", authConfig.getActiveProfile());
        model.addAttribute("activeMenu", ACTIVE_MENU);
    }
}
