package org.sopt.makers.internal.config;

import org.sopt.makers.internal.common.query.JPAQueryInspector;
import org.sopt.makers.internal.common.query.JPAQueryManageInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class JPAQueryManageConfig {

    @Bean
    @Profile("local")
    public JPAQueryManageInterceptor jpaQueryManageInterceptor() {
        return new JPAQueryManageInterceptor(new JPAQueryInspector());
    }
}
