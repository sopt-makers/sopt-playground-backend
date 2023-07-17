package org.sopt.makers.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.domain.InternalMemberDetails;
import org.sopt.makers.internal.domain.soulmate.Soulmate;
import org.sopt.makers.internal.domain.soulmate.SoulmateMissionHistory;
import org.sopt.makers.internal.domain.soulmate.SoulmateState;
import org.sopt.makers.internal.dto.soulmate.*;
import org.sopt.makers.internal.service.SoulmateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/soulmate")
@SecurityRequirement(name = "Authorization")
@Tag(name = "소울메이트 관련 API", description = "소울메이트와 관련 API들")
public class SoulmateController {
    private final SoulmateService soulmateService;

    @Operation(summary = "Soulmate 매칭 요창하기", description = "Matching Ready 상태로 바꾸기")
    @PostMapping("/start/matching")
    public ResponseEntity<SoulmateMatchingResponse> requestSoulmateMatching (
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
    ) {
        val soulmateMatchingInfo = soulmateService.readyToMatching(memberDetails.getId());
        soulmateService.tryMatching();
        return ResponseEntity.status(HttpStatus.OK).body(
                new SoulmateMatchingResponse(soulmateMatchingInfo.state(), soulmateMatchingInfo.soulmateId())
        );
    }

    @Operation(summary = "Soulmate 테스트 매칭 요창하기", description = "Matching Ready 상태로 바꾸기")
    @PostMapping("/start/test/matching")
    public ResponseEntity<SoulmateMatchingResponse> requestTestSoulmateMatching (
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
    ) {
        val soulmateMatchingInfo = soulmateService.readyToMatching(memberDetails.getId());
        soulmateService.tryTestMatching();
        return ResponseEntity.status(HttpStatus.OK).body(
                new SoulmateMatchingResponse(soulmateMatchingInfo.state(), soulmateMatchingInfo.soulmateId())
        );    }

    @Operation(summary = "Soulmate 미션하기")
    @PostMapping("/{id}/mission")
    public ResponseEntity<MissionResponse> doSoulmateMission (
            @RequestBody MissionUpdateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
    ) {
        val soulmate = soulmateService.missionResponded(request, memberDetails.getId());
        val missionSequence = soulmate.getMissionSequence();
        val hint = soulmateService.getSoulmateHint(soulmate.getOpponentId(), missionSequence);
        return ResponseEntity.status(HttpStatus.OK).body(new MissionResponse(soulmate.getState(), hint));
    }

    @Operation(summary = "Soulmate 히스토리 가져오기")
    @GetMapping("/{id}/histories")
    public ResponseEntity<List<Soulmate>> getHistories (
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
    ) {
        val histories = soulmateService.getSoulmateHistories(memberDetails.getId());
        return ResponseEntity.status(HttpStatus.OK).body(histories);
    }

    @Operation(summary = "Soulmate 미션 히스토리 가져오기")
    @GetMapping("/{id}/mission/histories")
    public ResponseEntity<List<SoulmateMissionHistory>> getMissionHistories (
            @PathVariable(value = "id") Long soulmateId,
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
    ) {
        val missionHistories = soulmateService.getMissionHistories(memberDetails.getId(), soulmateId);
        return ResponseEntity.status(HttpStatus.OK).body(missionHistories);
    }

    @Operation(summary = "Soulmate 상태 체크", description = "Disconnected check를 여기서 함")
    @GetMapping("/{id}/state")
    public ResponseEntity<SoulmateStateResponse> checkState (
            @PathVariable(value = "id") Long soulmateId,
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
    ) {
        val soulmate = soulmateService.checkState(memberDetails.getId(), soulmateId);
        return ResponseEntity.status(HttpStatus.OK).body(
                new SoulmateStateResponse(
                        soulmate.getId(), soulmate.getOpponentId(), soulmate.getStateModifiedAt(), soulmate.getState(), soulmate.getMissionSequence()
                )
        );
    }

    @Operation(summary = "Soulmate 동의")
    @PostMapping("/agree")
    public ResponseEntity<SoulmateResponse> agreeSoulmate (
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
    ) {
        soulmateService.agreeToSoulmate(memberDetails.getId());
        return ResponseEntity.status(HttpStatus.OK).body(new SoulmateResponse(true, null, null));
    }

    @Operation(summary = "Soulmate 동의 해제")
    @PostMapping("/disagree")
    public ResponseEntity<SoulmateResponse> disagreeSoulmate (
            @Parameter(hidden = true) @AuthenticationPrincipal InternalMemberDetails memberDetails
    ) {
        soulmateService.disagreeToSoulmate(memberDetails.getId());
        return ResponseEntity.status(HttpStatus.OK).body(new SoulmateResponse(true, null, null));
    }


}
