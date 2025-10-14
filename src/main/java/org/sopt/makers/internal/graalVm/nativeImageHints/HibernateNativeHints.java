package org.sopt.makers.internal.graalVm.nativeImageHints;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

/**
 * Hibernate를 위한 GraalVM Native Image 힌트
 *
 * Hibernate 6.2+는 바이트코드 향상 없이도 지연 로딩을 지원하지만,
 * GraalVM Native Image에서는 추가 힌트가 필요합니다.
 */
@Configuration
@ImportRuntimeHints(HibernateNativeHints.HibernateRuntimeHints.class)
public class HibernateNativeHints {

    static class HibernateRuntimeHints implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            // Hibernate 프록시 클래스들
            registerHibernateProxyClasses(hints);

            // 배열 타입들 (QueryDSL에서 배열 파라미터를 사용하는 경우)
            registerArrayTypes(hints);
        }

        private void registerHibernateProxyClasses(RuntimeHints hints) {
            String[] hibernateClasses = {
                "org.hibernate.proxy.HibernateProxy",
                "org.hibernate.proxy.LazyInitializer",
                "org.hibernate.proxy.pojo.bytebuddy.ByteBuddyInterceptor",
                "org.hibernate.bytecode.enhance.spi.interceptor.EnhancementAsProxyLazinessInterceptor",
                "org.hibernate.engine.spi.PersistentAttributeInterceptable",
                "org.hibernate.engine.spi.PersistentAttributeInterceptor"
            };

            for (String className : hibernateClasses) {
                try {
                    Class<?> clazz = Class.forName(className);
                    hints.reflection().registerType(
                        clazz,
                        MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_PUBLIC_METHODS,
                        MemberCategory.INVOKE_DECLARED_METHODS,
                        MemberCategory.DECLARED_FIELDS,
                        MemberCategory.PUBLIC_FIELDS
                    );
                } catch (ClassNotFoundException e) {
                    // 클래스가 없으면 무시
                }
            }
        }

        private void registerArrayTypes(RuntimeHints hints) {
            // QueryDSL과 Hibernate에서 사용하는 배열 타입들
            Class<?>[] arrayTypes = {
                String[].class,
                Long[].class,
                Integer[].class,
                Object[].class
            };

            for (Class<?> arrayType : arrayTypes) {
                hints.reflection().registerType(
                    arrayType,
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS
                );
            }
        }
    }
}
