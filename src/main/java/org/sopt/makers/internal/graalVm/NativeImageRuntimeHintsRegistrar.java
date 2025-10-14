package org.sopt.makers.internal.graalVm;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

/**
 * GraalVM Native Image를 위한 런타임 힌트 등록 (QueryDSL Core 전용)
 *
 * Spring Boot 3의 AOT 엔진이 대부분의 클래스를 자동으로 처리합니다:
 * - 일반 DTO/DAO/Vo: @Reflective 어노테이션으로 자동 처리
 * - 복잡한 타입을 가진 QueryDSL Projection DTO: QueryDslProjectionNativeHints에서 처리
 * - Hibernate Proxy: HibernateNativeHints에서 처리
 * - Feign Client: FeignNativeHints에서 처리
 *
 * 이 클래스는 QueryDSL Core 라이브러리 타입만 처리합니다.
 */
@Configuration
@ImportRuntimeHints(NativeImageRuntimeHintsRegistrar.Registrar.class)
public class NativeImageRuntimeHintsRegistrar {

    static class Registrar implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            // 1. QueryDSL 관련 타입 등록
            registerQueryDslTypes(hints);

            // 2. 패키지 단위로 리소스 등록
            registerResourcePatterns(hints);
        }

        private void registerQueryDslTypes(RuntimeHints hints) {
            // QueryDSL Core 타입들
            String[] queryDslClasses = {
                "com.querydsl.core.types.ConstructorExpression",
                "com.querydsl.core.types.Projections",
                "com.querydsl.core.types.Expression",
                "com.querydsl.core.types.dsl.Expressions"
            };

            for (String className : queryDslClasses) {
                hints.reflection().registerType(
                    TypeReference.of(className),
                    builder -> builder.withMembers(
                        MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                        MemberCategory.INVOKE_PUBLIC_METHODS
                    )
                );
            }

            // QueryDSL Projection 패키지 전체를 리플렉션에 등록
            // Spring Boot AOT가 @QueryProjection을 자동으로 감지하지만, 추가 보장
            hints.reflection().registerType(
                TypeReference.of("com.querydsl.core.annotations.QueryProjection"),
                builder -> builder.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS)
            );
        }

        private void registerResourcePatterns(RuntimeHints hints) {
            // 리소스 패턴 등록 (필요한 경우)
            hints.resources().registerPattern("META-INF/native-image/*");
            hints.resources().registerPattern("static/*");
        }
    }
}
