package org.sopt.makers.internal.controller.filter;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.domain.InternalTokenManager;
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
        if (jwtToken != null && tokenManager.verifyAuthToken(jwtToken)) {
            val auth = tokenManager.getAuthentication(jwtToken);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request, response);
    }

    private String parseJwt (HttpServletRequest request) {
        val headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth)) return headerAuth;
        return null;
    }
}
