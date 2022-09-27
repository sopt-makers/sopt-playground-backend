package org.sopt.makers.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.dto.MemberSaveRequest;
import org.sopt.makers.internal.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class MemberController {
    private final MemberService memberService;

    @Operation(summary = "유저 id로 조회 API")
    @GetMapping("/{id}")
    public ResponseEntity<Member> getMember (@PathVariable Long id) {
        val member = memberService.getMemberById(id);
        return ResponseEntity.status(HttpStatus.OK).body(member);
    }

    @Operation(summary = "유저 이름으로 조회 API")
    @GetMapping("/search")
    public ResponseEntity<Member> getMemberByName (@RequestParam String name) {
        val member = memberService.getMemberByName(name);
        return ResponseEntity.status(HttpStatus.OK).body(member);
    }

    @Operation(summary = "유저 생성 API")
    @PostMapping("")
    public ResponseEntity<Member> getMemberByName (@RequestBody MemberSaveRequest request) {
        val member = memberService.createMember(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(member);
    }

}
