package org.sopt.makers.internal.config;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.controller.filter.JwtAuthenticationFilter;
import org.sopt.makers.internal.controller.filter.JwtExceptionFilter;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
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

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtExceptionFilter jwtExceptionFilter;
    private final WebEndpointProperties webEndpointProperties;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        String actuatorEndpoint = webEndpointProperties.getBasePath();

        return http.antMatcher("/**")
                .httpBasic().disable()
                .formLogin().disable()
                .csrf().disable()
                .cors().configurationSource(corsConfigurationSource())
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                    .authorizeHttpRequests()
                    .antMatchers("/v3/api-docs/**", "/swagger-ui.html", "/webjars/swagger-ui/**", "/swagger-ui/**", "/makers/**", actuatorEndpoint+"/prometheus", actuatorEndpoint+"/metrics").permitAll()
                .and()
                    .authorizeHttpRequests()
                        .antMatchers("/internal/api/v1/projects/**", "/internal/api/v1/members/**", "/internal/api/v1/sopticles/**", "/internal/api/v1/profile",
                                "/api/v1/presigned-url", "/api/v1/users/**", "/api/v1/projects/**").hasAuthority("Member")
                .and()
                    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                    .addFilterBefore(jwtExceptionFilter, JwtAuthenticationFilter.class)
                .build();
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
