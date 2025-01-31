package org.sopt.makers.internal.config;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.sopt.makers.internal.common.query.JPAQueryManageInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

	private final JPAQueryManageInterceptor jpaQueryManageInterceptor;

	@Override
	public void addInterceptors(@NotNull InterceptorRegistry registry) {
		if (jpaQueryManageInterceptor != null) {
			registry.addInterceptor(jpaQueryManageInterceptor)
					.addPathPatterns("/**");
		}
	}
}
