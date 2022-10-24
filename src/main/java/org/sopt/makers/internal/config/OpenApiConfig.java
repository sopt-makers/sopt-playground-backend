package org.sopt.makers.internal.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.stereotype.Component;

@OpenAPIDefinition(
        servers = {
                @Server(url = "http://localhost:8080"),
                @Server(url = "http://playground.dev.sopt.org:8080")
        },
        info = @Info(
                title = "SOPT Makers Internal team API",
                version = "v1",
                description = "메이커스 인터널 팀 API입니다."
        )
)
@SecurityScheme(
        name = "Authorization",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER
)
@Component
public class OpenApiConfig {}
