package org.sopt.makers.internal.common.auth.jwtFilter;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.internal.InternalTokenManager;
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

@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final InternalTokenManager tokenManager;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws ServletException, IOException {
        val jwtToken = parseJwt(request);
        val isTokenAvailable = checkJwtAvailable(jwtToken);
        val uri = request.getRequestURI();
        if ((uri.startsWith("/api") || uri.startsWith("/internal"))
                && !uri.contains("idp") && !uri.contains("registration") && !uri.contains("change")) {
            if (!isTokenAvailable)
                throw new WrongAccessTokenException("Token is empty or not verified");
        }

        if (isTokenAvailable) {
            val auth = tokenManager.getAuthentication(jwtToken);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request, response);
    }

    private boolean checkJwtAvailable (String jwtToken) {
        return jwtToken != null && tokenManager.verifyAuthToken(jwtToken);
    }

    private String parseJwt (HttpServletRequest request) {
        val headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth)) return headerAuth;
        return null;
    }
}
