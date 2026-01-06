package org.sopt.makers.internal.auth.jwtFilter;

import io.github.resilience4j.core.lang.NonNull;
import org.sopt.makers.internal.auth.jwt.exception.JwtException;
import org.sopt.makers.internal.exception.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtExceptionFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull final HttpServletRequest httpServletRequest,
            @NonNull final HttpServletResponse httpServletResponse,
            @NonNull final FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        } catch(UnauthorizedException | JwtException e){
            httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
            httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
            httpServletResponse.setCharacterEncoding("UTF-8");
            httpServletResponse.getWriter().write(e.getMessage());
        }
    }
}
