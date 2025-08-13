package org.sopt.makers.internal.auth.security.config;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.auth.jwtFilter.JwtAuthenticationFilter;
import org.sopt.makers.internal.auth.jwtFilter.JwtExceptionFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtExceptionFilter jwtExceptionFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .httpBasic().disable()
            .formLogin().disable()
            .csrf().disable()
            .cors().configurationSource(corsConfigurationSource())
        .and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
            .authorizeHttpRequests()
                .antMatchers(
                        "/v3/api-docs/**",
                        "/swagger-ui.html",
                        "/webjars/swagger-ui/**",
                        "/swagger-ui/**",
                        "/makers/**",
                        "/internal/api/v1/**",
                        "/api/v1/admin/**"
                ).permitAll()

//                .antMatchers(
//                        "/internal/api/v1/projects/**",
//                        "/internal/api/v1/members/**",
//                        "/internal/api/v1/sopticles/**",
//                        "/internal/api/v1/profile",
//                        "/api/v1/presigned-url"
//                ).hasAuthority("MEMBER")

        .and()
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtExceptionFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        val configuration = new CorsConfiguration();

        configuration.addAllowedOrigin("*");
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setAllowCredentials(false);

        val source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
