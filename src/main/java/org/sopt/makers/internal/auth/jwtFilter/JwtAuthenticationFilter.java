package org.sopt.makers.internal.auth.jwtFilter;

import java.io.IOException;
import java.util.Objects;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.auth.jwt.service.JwtAuthenticationService;
import org.sopt.makers.internal.auth.security.authentication.MakersAuthentication;
import org.sopt.makers.internal.exception.WrongAccessTokenException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JwtAuthenticationFilter : 매 요청마다 실행되는 JWT 필터
 * 흐름:
 * Authoriztion 헤더에서 JWT 추출 -> JwtAuthenticationService를 통해 토큰을 검증 -> 인증 정보를 SecurityContext에 저장
 */
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtAuthenticationService jwtAuthenticationService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();

        // 해당 경로는 인증 로직 무시
        if (uri.startsWith("/swagger-ui")
                || uri.startsWith("/v3/api-docs")
                || uri.startsWith("/swagger-resources")
                || uri.startsWith("/webjars")
                || uri.startsWith("/makers")
                || uri.startsWith("/internal/api/v1")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 헤더에서 Authorization 값 추출 (eyJhbxor...)
        String authorizationToken = parseJwt(request);
        checkJwtAvailable(authorizationToken);

        if ((uri.startsWith("/api") || uri.startsWith("/internal"))
                && !uri.contains("idp")) {
            MakersAuthentication authentication = jwtAuthenticationService.authenticate(authorizationToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 필터체인으로 요청 전달
        filterChain.doFilter(request, response);
    }

    private void checkJwtAvailable (String jwtToken) {
        if (Objects.isNull(jwtToken)) {
            throw new WrongAccessTokenException("Token is empty or not verified");
        }

    }

    private String parseJwt (HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth)) return headerAuth;
        return null;
    }
}
