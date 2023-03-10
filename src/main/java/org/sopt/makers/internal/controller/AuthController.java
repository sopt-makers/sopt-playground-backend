package org.sopt.makers.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.dto.auth.*;
import org.sopt.makers.internal.exception.AuthFailureException;
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

    @Operation(summary = "SSO Code Test API", description = "SSO 코드 발급을 위한 테스트 엔드포인트")
    @PostMapping("/idp/sso/code")
    public ResponseEntity<CodeResponse> createCode (@RequestBody CodeRequest request) {
        val code = authService.createCode(request.accessToken());
        return ResponseEntity.status(HttpStatus.OK).body(new CodeResponse(code));
    }

    @Operation(summary = "SSO Access Token API", description = "SSO AccessToken 발급을 위한 엔드포인트")
    @PostMapping("/idp/sso/auth")
    public ResponseEntity<AccessTokenResponse> ssoAccessToken (@RequestBody AuthRequest request) {
        val accessToken = authService.authByCode(request.code());
        return ResponseEntity.status(HttpStatus.OK).body(new AccessTokenResponse(accessToken));
    }

    @Operation(summary = "Facebook auth API", description = "페이스북으로 로그인")
    @PostMapping("/idp/facebook/auth")
    public ResponseEntity<AccessTokenResponse> authByFacebook (@RequestBody AuthRequest request) {
        val accessToken = authService.authByFb(request.code());
        return ResponseEntity.status(HttpStatus.OK).body(new AccessTokenResponse(accessToken));
    }

    @Operation(summary = "Facebook register API")
    @PostMapping("/idp/facebook/register")
    public ResponseEntity<AccessTokenResponse> registerByFacebook (@RequestBody RegisterByFacebookRequest request) {
        val accessToken = authService.registerByFb(request.registerToken(), request.code());
        return ResponseEntity.status(HttpStatus.OK).body(new AccessTokenResponse(accessToken));
    }

    @Operation(summary = "Google auth API", description = "구글로 로그인")
    @PostMapping("/idp/google/auth")
    public ResponseEntity<AccessTokenResponse> authByGoogle (@RequestBody AuthRequest request) {
        val accessToken = authService.authByGoogle(request.code());
        return ResponseEntity.status(HttpStatus.OK).body(new AccessTokenResponse(accessToken));
    }

    @Operation(summary = "Google register API")
    @PostMapping("/idp/google/register")
    public ResponseEntity<AccessTokenResponse> registerByGoogle (@RequestBody RegisterByFacebookRequest request) {
        val accessToken = authService.registerByGoogle(request.registerToken(), request.code());
        return ResponseEntity.status(HttpStatus.OK).body(new AccessTokenResponse(accessToken));
    }

    @Operation(summary = "register 토큰으로 자기 자신 확인 API")
    @PostMapping("/registration/info")
    public ResponseEntity<RegisterTokenInfoResponse> checkRegisterToken (@RequestBody RegisterTokenInfoRequest request) {
        val memberHistory = authService.findMemberByRegisterToken(request.registerToken())
                .orElseThrow(() -> new ForbiddenClientException("SOPT Member History를 찾을 수 없습니다."));
        return ResponseEntity.status(200).body(new RegisterTokenInfoResponse(memberHistory.getName(), memberHistory.getGeneration()));
    }

    @Operation(summary = "register 토큰이 전송되는 email 발송 API")
    @PostMapping("/registration/email")
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

    @Operation(summary = "Get 6 numbers code")
    @PostMapping("/registration/sms/code")
    public ResponseEntity<String> sendRegistrationSms (@RequestBody RegistrationPhoneRequest request) {
        if (!request.phone().startsWith("010")) throw new AuthFailureException("전화번호가 옳지 않습니다.");
        authService.sendSixNumberSmsCode(request.phone());
        return ResponseEntity.ok("Success!");
    }

    @Operation(summary = "Get registerToken by 6 number Code")
    @PostMapping("/registration/sms/token")
    public ResponseEntity<RegisterTokenResponse> getRegistrationToken (@RequestBody RegistrationTokenBySmsRequest request) {
        if (request.sixNumberCode().length() != 6) throw new AuthFailureException("6자리 코드가 옳지 않습니다.");
        val registerToken = authService.getRegisterTokenBySixNumberCode(request.sixNumberCode());
        return ResponseEntity.ok(new RegisterTokenResponse(registerToken));
    }
}
