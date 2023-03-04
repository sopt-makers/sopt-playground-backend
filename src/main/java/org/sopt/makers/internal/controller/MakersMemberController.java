package org.sopt.makers.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.config.AuthConfig;
import org.sopt.makers.internal.dto.member.MemberProfileResponse;
import org.sopt.makers.internal.exception.WrongSecretHeaderException;
import org.sopt.makers.internal.mapper.MemberMapper;
import org.sopt.makers.internal.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("")
@Tag(name = "Makers 만든 사람들 관련 API")
public class MakersMemberController {

    private final MemberService memberService;
    private final MemberMapper memberMapper;
    private final AuthConfig config;

    @Operation(summary = "메이커스 만든 사람들을 위한 전체 조회 API")
    @GetMapping("/makers/profile")
    public ResponseEntity<List<MemberProfileResponse>> getUserProfiles(
            @RequestHeader(name = "X-Admin-Access-Secret") String secret
    ) {
        if (!Objects.equals(secret, config.getMakersSecretKey())) throw new WrongSecretHeaderException("키가 일치하지 않습니다.");
        val members = memberService.getAllMemberProfiles();
        val responses = members.stream().map(memberMapper::toProfileResponse).collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }
}
