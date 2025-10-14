package org.sopt.makers.internal.graalVm.nativeImageHints;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

/**
 * QueryDSL Projections.constructor()를 위한 GraalVM Native Image 힌트
 *
 * @Reflective 어노테이션만으로는 복잡한 타입을 가진 Record의
 * canonical constructor가 제대로 등록되지 않는 경우가 있어 명시적 등록이 필요합니다.
 *
 * 특히 다음 경우에 명시적 등록이 필요:
 * - List<Enum> 타입 파라미터를 가진 경우
 * - 배열(String[], Long[] 등) 타입 파라미터를 가진 경우
 * - JPA Converter가 적용된 필드를 Projection하는 경우
 */
@Configuration
@ImportRuntimeHints(QueryDslProjectionNativeHints.QueryDslProjectionRuntimeHints.class)
public class QueryDslProjectionNativeHints {

    static class QueryDslProjectionRuntimeHints implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            // QueryDSL Projection으로 사용되는 DTO 클래스들
            // 복잡한 타입(List<Enum> 등)을 포함하는 경우 명시적 등록
            registerComplexProjectionDtos(hints);
        }

        private void registerComplexProjectionDtos(RuntimeHints hints) {
            // 복잡한 타입(List<Enum>, 배열 등)을 가진 DTO들
            String[] complexDtoClasses = {
                "org.sopt.makers.internal.coffeechat.dto.request.CoffeeChatInfoDto",
                "org.sopt.makers.internal.coffeechat.dto.request.RecentCoffeeChatInfoDto",
                "org.sopt.makers.internal.member.dto.MemberProfileProjectDao",
                "org.sopt.makers.internal.community.dto.CategoryPostMemberDao",
                "org.sopt.makers.internal.community.dto.PostCategoryDao",
                "org.sopt.makers.internal.community.dto.CommentDao",
                "org.sopt.makers.internal.project.dto.dao.ProjectDao",
                "org.sopt.makers.internal.project.dto.dao.ProjectLinkDao",
                "org.sopt.makers.internal.project.dto.dao.ProjectMemberDao"
            };

            for (String className : complexDtoClasses) {
                hints.reflection().registerType(
                    TypeReference.of(className),
                    builder -> builder.withMembers(
                        MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.PUBLIC_FIELDS,
                        MemberCategory.DECLARED_FIELDS
                    )
                );
            }
        }
    }
}
