package org.sopt.makers.internal.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@EnableFeignClients(basePackages = "org.sopt.makers.internal.external")
@Configuration
public class FeignConfig {
}