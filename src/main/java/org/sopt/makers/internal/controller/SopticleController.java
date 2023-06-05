package org.sopt.makers.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.domain.InternalMemberDetails;
import org.sopt.makers.internal.dto.sopticle.SopticleSaveRequest;
import org.sopt.makers.internal.service.SopticleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sopticles")
@SecurityRequirement(name = "Authorization")
@Tag(name = "솝티클 관련 API", description = "솝티클과 관련 API들")
public class SopticleController {
    private final SopticleService sopticleService;
    @Operation(summary = "Sopticle 생성 API")
    @PostMapping("")
    public ResponseEntity<Map<String, Boolean>> createProject (
            @RequestBody SopticleSaveRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
    ) {
        sopticleService.createSopticle(request, memberDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("success", true));
    }
}
