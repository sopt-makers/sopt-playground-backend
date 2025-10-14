package org.sopt.makers.internal.graalVm.nativeImageHints;

import feign.Capability;
import feign.Client;
import feign.Contract;
import feign.ExceptionPropagationPolicy;
import feign.InvocationHandlerFactory;
import feign.Logger;
import feign.QueryMapEncoder;
import feign.Request;
import feign.RequestInterceptor;
import feign.ResponseInterceptor;
import feign.Retryer;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

/**
 * Feign Client를 위한 GraalVM Native Image 힌트
 *
 * 이 클래스는 모든 Spring Boot 프로젝트에서 재사용 가능합니다.
 * Feign을 사용하는 프로젝트에 이 클래스만 복사하면 됩니다.
 */
@Configuration
@ImportRuntimeHints(FeignNativeHints.FeignRuntimeHints.class)
public class FeignNativeHints {

    static class FeignRuntimeHints implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            // Feign 핵심 클래스들
            registerFeignCoreClasses(hints);

            // Feign Capability의 모든 enrich 메서드
            registerCapabilityMethods(hints);

            // Spring Cloud OpenFeign 클래스들
            registerSpringCloudFeignClasses(hints);
        }

        private void registerFeignCoreClasses(RuntimeHints hints) {
            Class<?>[] coreClasses = {
                Capability.class,
                Client.class,
                Contract.class,
                Decoder.class,
                Encoder.class,
                ErrorDecoder.class,
                InvocationHandlerFactory.class,
                Logger.class,
                QueryMapEncoder.class,
                RequestInterceptor.class,
                ResponseInterceptor.class,
                ResponseInterceptor.Chain.class,
                Retryer.class,
                ExceptionPropagationPolicy.class,
                Request.Options.class
            };

            for (Class<?> clazz : coreClasses) {
                hints.reflection().registerType(
                    clazz,
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.INVOKE_PUBLIC_METHODS,
                    MemberCategory.INVOKE_DECLARED_METHODS,
                    MemberCategory.DECLARED_FIELDS,
                    MemberCategory.PUBLIC_FIELDS
                );
            }
        }

        private void registerCapabilityMethods(RuntimeHints hints) {
            // Capability의 모든 enrich 메서드를 명시적으로 등록
            try {
                hints.reflection().registerMethod(
                    Capability.class.getMethod("enrich", RequestInterceptor.class),
                    ExecutableMode.INVOKE
                );
                hints.reflection().registerMethod(
                    Capability.class.getMethod("enrich", ResponseInterceptor.Chain.class),
                    ExecutableMode.INVOKE
                );
                hints.reflection().registerMethod(
                    Capability.class.getMethod("enrich", Client.class),
                    ExecutableMode.INVOKE
                );
                hints.reflection().registerMethod(
                    Capability.class.getMethod("enrich", Decoder.class),
                    ExecutableMode.INVOKE
                );
                hints.reflection().registerMethod(
                    Capability.class.getMethod("enrich", Encoder.class),
                    ExecutableMode.INVOKE
                );
                hints.reflection().registerMethod(
                    Capability.class.getMethod("enrich", Contract.class),
                    ExecutableMode.INVOKE
                );
                hints.reflection().registerMethod(
                    Capability.class.getMethod("enrich", Logger.class),
                    ExecutableMode.INVOKE
                );
                hints.reflection().registerMethod(
                    Capability.class.getMethod("enrich", Logger.Level.class),
                    ExecutableMode.INVOKE
                );
                hints.reflection().registerMethod(
                    Capability.class.getMethod("enrich", InvocationHandlerFactory.class),
                    ExecutableMode.INVOKE
                );
                hints.reflection().registerMethod(
                    Capability.class.getMethod("enrich", QueryMapEncoder.class),
                    ExecutableMode.INVOKE
                );
                hints.reflection().registerMethod(
                    Capability.class.getMethod("enrich", ErrorDecoder.class),
                    ExecutableMode.INVOKE
                );
                hints.reflection().registerMethod(
                    Capability.class.getMethod("enrich", Request.Options.class),
                    ExecutableMode.INVOKE
                );
                hints.reflection().registerMethod(
                    Capability.class.getMethod("enrich", Retryer.class),
                    ExecutableMode.INVOKE
                );
                hints.reflection().registerMethod(
                    Capability.class.getMethod("enrich", ExceptionPropagationPolicy.class),
                    ExecutableMode.INVOKE
                );
            } catch (NoSuchMethodException e) {
                // Feign 버전에 따라 일부 메서드가 없을 수 있음
            }
        }

        private void registerSpringCloudFeignClasses(RuntimeHints hints) {
            // Spring Cloud OpenFeign 클래스들
            String[] springCloudClasses = {
                "org.springframework.cloud.openfeign.FeignClientFactoryBean",
                "org.springframework.cloud.openfeign.FeignContext",
                "org.springframework.cloud.openfeign.support.SpringMvcContract",
                "org.springframework.cloud.openfeign.support.SpringEncoder",
                "org.springframework.cloud.openfeign.support.SpringDecoder",
                "org.springframework.cloud.openfeign.support.ResponseEntityDecoder",
                "org.springframework.cloud.openfeign.FeignLoggerFactory",
                "org.springframework.cloud.openfeign.clientconfig.FeignClientConfigurer"
            };

            for (String className : springCloudClasses) {
                try {
                    Class<?> clazz = Class.forName(className);
                    hints.reflection().registerType(
                        clazz,
                        MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_PUBLIC_METHODS,
                        MemberCategory.INVOKE_DECLARED_METHODS
                    );
                } catch (ClassNotFoundException e) {
                    // 클래스가 없으면 무시
                }
            }
        }
    }
}
