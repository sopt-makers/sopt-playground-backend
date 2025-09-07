package org.sopt.makers.internal.auth.jwt.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.auth.common.code.FailureCode;
import org.springframework.http.HttpStatus;

import static lombok.AccessLevel.*;

@Getter
@RequiredArgsConstructor(access = PRIVATE)
public enum JwtFailure implements FailureCode {
    JWT_MISSING_AUTH_HEADER(HttpStatus.UNAUTHORIZED, "인증 헤더가 존재하지 않습니다."),
    JWT_PARSE_FAILED(HttpStatus.UNAUTHORIZED, "잘못된 형식의 JWT입니다."),
    JWT_INVALID_CLAIMS(HttpStatus.UNAUTHORIZED, "JWT의 클레임이 유효하지 않습니다."),
    JWT_VERIFICATION_FAILED(HttpStatus.UNAUTHORIZED, "JWT 검증에 실패했습니다."),
    JWT_KID_MISSING(HttpStatus.UNAUTHORIZED, "JWT 헤더의 kid가 없습니다."),
    JWT_ALG_MISMATCH(HttpStatus.UNAUTHORIZED, "지원하지 않는 서명 알고리즘입니다. (RS256만 허용)");

    private final HttpStatus status;
    private final String message;
}