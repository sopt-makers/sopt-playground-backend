package org.sopt.makers.internal.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.member.dto.response.MakersMemberProfileResponse;
import org.sopt.makers.internal.member.mapper.MemberMapper;
import org.sopt.makers.internal.member.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("")
@Tag(name = "Makers 만든 사람들 관련 API")
public class MakersMemberController {

    private final MemberService memberService;
    private final MemberMapper memberMapper;

    @Operation(summary = "메이커스 만든 사람들을 위한 전체 조회 API")
    @GetMapping("/makers/profile")
    public ResponseEntity<List<MakersMemberProfileResponse>> getUserProfiles() {
        List<MakersMemberProfileResponse> responses = memberService.getAllMakersMembersProfiles();
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }
}
