package org.sopt.makers.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.config.AuthConfig;
import org.sopt.makers.internal.dto.auth.*;
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
    private final AuthConfig authConfig;

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
    public ResponseEntity<AccessTokenResponse> registerByFacebook (@RequestBody RegisterRequest request) {
        String accessToken;
        if (request.registerToken().equals(authConfig.getMagicRegisterToken())) {
            accessToken = authService.registerByFbAndMagicRegisterToken(request.registerToken(), request.code());
        } else {
            accessToken = authService.registerByFb(request.registerToken(), request.code());
        }
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
    public ResponseEntity<AccessTokenResponse> registerByGoogle (@RequestBody RegisterRequest request) {
        String accessToken;
        if (request.registerToken().equals(authConfig.getMagicRegisterToken())) {
            accessToken = authService.registerByGoogleAndMagicRegisterToken(request.registerToken(), request.code());
        } else {
            accessToken = authService.registerByGoogle(request.registerToken(), request.code());
        }
        return ResponseEntity.status(HttpStatus.OK).body(new AccessTokenResponse(accessToken));
    }

    @Operation(summary = "Apple auth API", description = "애플로 로그인")
    @PostMapping("/idp/apple/auth")
    public ResponseEntity<AccessTokenResponse> authByApple (@RequestBody AuthRequest request) {
        val accessToken = authService.authByApple(request.code());
        return ResponseEntity.status(HttpStatus.OK).body(new AccessTokenResponse(accessToken));
    }

    @Operation(summary = "Apple register API")
    @PostMapping("/idp/apple/register")
    public ResponseEntity<AccessTokenResponse> registerByApple (@RequestBody RegisterRequest request) {
        String accessToken;
        if (request.registerToken().equals(authConfig.getMagicRegisterToken())) {
            accessToken = authService.registerByAppleAndMagicRegisterToken(request.registerToken(), request.code());
        } else {
            accessToken = authService.registerByApple(request.registerToken(), request.code());
        }
        return ResponseEntity.status(HttpStatus.OK).body(new AccessTokenResponse(accessToken));
    }


    @Operation(summary = "register 토큰으로 자기 자신 확인 API")
    @PostMapping("/registration/info")
    public ResponseEntity<RegisterTokenInfoResponse> checkRegisterToken (@RequestBody RegisterTokenInfoRequest request) {
        if (request.registerToken().equals(authConfig.getMagicRegisterToken())) return ResponseEntity.status(200).body(new RegisterTokenInfoResponse("Tester", 32));
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
                    .body(new EmailResponse(false, "unknownError", "알 수 없는 이유로 이메일 발송에 실패했습니다."));
        };
    }

    @Operation(summary = "Get 6 numbers code")
    @PostMapping("/registration/sms/code")
    public ResponseEntity<SmsCodeResponse> sendRegistrationSms (@RequestBody RegistrationPhoneRequest request) {
        if (request.phone().equals(authConfig.getMagicNumber())) {
            val registerToken = authService.getRegisterTokenByMagicNumber();
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new SmsCodeResponse(true, null, null, true, registerToken));
        }

        if (!request.phone().startsWith("010")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new SmsCodeResponse(false, "wrongPhoneNumber", "잘못된 핸드폰 번호입니다.", false, null));
        }
        val status = authService.sendSixNumberSmsCode(request.phone());
        return switch (status) {
            case "success" -> ResponseEntity.status(HttpStatus.OK).body(new SmsCodeResponse(true, null, null, false, null));
            case "emptySoptUser" -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new SmsCodeResponse(false, status, "인증할 수 없는 유저입니다. 문의해주세요.", false, null));
            case "shouldRetry" -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new SmsCodeResponse(false, status, "재시도 해주세요.", false, null));
            case "alreadyTaken" -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new SmsCodeResponse(false, status, "이미 가입한 유저입니다.", false, null));
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new SmsCodeResponse(false, "unknownError", "알 수 없는 이유로 이메일 발송에 실패했습니다.", false, null));
        };
    }

    @Operation(summary = "Get registerToken by 6 number Code")
    @PostMapping("/registration/sms/token")
    public ResponseEntity<RegisterTokenBySmsResponse> getRegistrationToken (@RequestBody RegistrationTokenBySmsRequest request) {
        if (request.sixNumberCode().length() != 6) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new RegisterTokenBySmsResponse(false, "wrongCode", "잘못된 코드입니다.", null));
        }
        val registerToken = authService.getRegisterTokenBySixNumberCode(request.sixNumberCode());
        return ResponseEntity.ok(new RegisterTokenBySmsResponse(true, null, null, registerToken));
    }
}
