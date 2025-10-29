package org.sopt.makers.internal.hints;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

/**
 * Admin 페이지를 위한 GraalVM Native Image 힌트
 *
 * Thymeleaf 템플릿과 정적 리소스(CSS)를 Native Image에 포함시킵니다.
 */
@Configuration
@ImportRuntimeHints(AdminResourcesNativeHints.AdminResourcesRuntimeHints.class)
public class AdminResourcesNativeHints {

    static class AdminResourcesRuntimeHints implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            // Thymeleaf 템플릿 리소스 등록
            hints.resources().registerPattern("templates/admin/*.html");

            // 정적 리소스(CSS) 등록
            hints.resources().registerPattern("static/css/*.css");

            // Thymeleaf 관련 클래스 리플렉션 등록
            hints.reflection().registerType(
                org.thymeleaf.spring6.SpringTemplateEngine.class,
                hint -> hint.withMembers(
                    org.springframework.aot.hint.MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                    org.springframework.aot.hint.MemberCategory.INVOKE_PUBLIC_METHODS
                )
            );

            hints.reflection().registerType(
                org.thymeleaf.spring6.view.ThymeleafViewResolver.class,
                hint -> hint.withMembers(
                    org.springframework.aot.hint.MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                    org.springframework.aot.hint.MemberCategory.INVOKE_PUBLIC_METHODS
                )
            );
        }
    }
}
