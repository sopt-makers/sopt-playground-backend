package org.sopt.makers.internal.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.stereotype.Component;

@OpenAPIDefinition(
        servers = {
                @Server(url = "http://localhost:8080")
        },
        info = @Info(
                title = "SOPT Makers Internal team API",
                version = "v1",
                description = "메이커스 인터널 팀 API입니다."
        )
)
@Component
public class OpenApiConfig {}
