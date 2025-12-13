package org.sopt.makers.internal.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("!prod")
@OpenAPIDefinition(
        servers = {
                @Server(url = "https://playground-dev.sopt.org", description = "개발 환경"),
                @Server(url = "http://localhost:8080", description = "로컬 환경")
        },
        info = @Info(
                title = "SOPT Makers Internal team API",
                version = "v1",
                description = "메이커스 인터널 팀 API입니다."
        )
)
@SecurityScheme(
        name = "Authorization",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
@Component
public class OpenApiConfig {}
