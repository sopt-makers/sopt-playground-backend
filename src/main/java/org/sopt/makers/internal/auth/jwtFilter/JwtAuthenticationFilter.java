package org.sopt.makers.internal.auth.jwtFilter;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.auth.security.authentication.MakersAuthentication;
import org.sopt.makers.internal.auth.jwt.service.JwtAuthenticationService;
import org.sopt.makers.internal.exception.WrongAccessTokenException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JwtAuthenticationFilter : 매 요청마다 실행되는 JWT 필터
 * 흐름:
 * Authoriztion 헤더에서 JWT 추출 -> JwtAuthenticationService를 통해 토큰을 검증 -> 인증 정보를 SecurityContext에 저장
 */
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtAuthenticationService jwtAuthenticationService;
    private static final String ACCESS_TOKEN_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 헤더에서 Authorization 값 추출 (eyJhbxor...)
        String authorizationToken = parseJwt(request);

        // 예외처리 (Authorization 헤더가 없거나 Bearer 타입이 아닌 경우 에러 처리)
        checkJwtAvailable(authorizationToken);

        // JWT 추출, 토큰 검증 -> 인증 객체 생성
        String uri = request.getRequestURI();
        if ((uri.startsWith("/api") || uri.startsWith("/internal"))
                && !uri.contains("idp")) {
            MakersAuthentication authentication = jwtAuthenticationService.authenticate(authorizationToken);

            // 인증 정보 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 필터체인으로 요청 전달
        filterChain.doFilter(request, response);
    }

    private void checkJwtAvailable (String jwtToken) {
        if (jwtToken != null && !jwtToken.startsWith(ACCESS_TOKEN_PREFIX)) {
            throw new WrongAccessTokenException("Token is empty or not verified");
        }

    }

    private String parseJwt (HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth)) return headerAuth;
        return null;
    }
}
