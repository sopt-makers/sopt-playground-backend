package org.sopt.makers.internal.controller;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.dto.*;
import org.sopt.makers.internal.exception.ForbiddenClientException;
import org.sopt.makers.internal.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/idp/facebook/auth")
    public ResponseEntity<AccessTokenResponse> authByFacebook (@RequestBody AuthByFacebookRequest request) {
        val accessToken = authService.authByFb(request.code());
        return ResponseEntity.status(HttpStatus.OK).body(new AccessTokenResponse(accessToken));
    }

    @PostMapping("/idp/facebook/register")
    public ResponseEntity<AccessTokenResponse> registerByFacebook (@RequestBody RegisterByFacebookRequest request) {
        val accessToken = authService.registerByFb(request.registerToken(), request.code());
        return ResponseEntity.status(HttpStatus.OK).body(new AccessTokenResponse(accessToken));
    }

    @PostMapping("/register/checkToken")
    public ResponseEntity<RegisterTokenInfoResponse> checkRegisterToken (@RequestBody RegisterTokenInfoRequest request) {
        val member = authService.findMemberByRegisterToken(request.registerToken())
                .orElseThrow(() -> new ForbiddenClientException("멤버를 찾을 수 없습니다."));
        return ResponseEntity.status(200).body(new RegisterTokenInfoResponse(member.getName(), member.getGeneration()));
    }

    @PostMapping("/register/sendEmail")
    public ResponseEntity<EmailResponse> sendRegistrationEmail (@RequestBody RegistrationEmailRequest request) {
        val status = authService.sendRegisterLinkByEmail(request.email());
        return switch (status) {
            case "success" -> ResponseEntity.status(HttpStatus.OK).body(new EmailResponse(true, null, null));
            case "cannotSendEmail" -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new EmailResponse(false, status, "이메일 발송에 실패했습니다."));
            case "alreadyTaken" -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new EmailResponse(false, status, "가입할 수 없는 이메일입니다."));
            case "invalidEmail" -> ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new EmailResponse(false, status, "가입할 수 없는 이메일입니다."));
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new EmailResponse(false, "UnknownError", "알 수 없는 이유로 이메일 발송에 실패했습니다."));
        };
    }
}
