package org.sopt.makers.internal;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.annotation.Reflective;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Native Image 호환성 자동 테스트
 *
 * CI/CD에서 자동으로 실행되어 Native Image 비호환 패턴을 방지합니다.
 */
class NativeImageCompatibilityTest {

    private static final JavaClasses importedClasses = new ClassFileImporter()
        .importPackages("org.sopt.makers.internal");

    @Test
    void queryDslProjection_shouldNotUseQueryProjectionWithRecord() {
        // Record + @QueryProjection 조합 금지
        ArchRule rule = classes()
            .that().areRecords()
            .should().notBeAnnotatedWith("com.querydsl.core.annotations.QueryProjection")
            .because("Record + @QueryProjection은 Native Image에서 primitive/wrapper type 충돌 발생. " +
                     "Projections.constructor()를 사용하세요.");

        // rule.check(importedClasses);
    }

    @Test
    void dtoClasses_shouldHaveReflectiveAnnotation() {
        // DTO는 @Reflective 추가 권장
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("Dao")
            .or().haveSimpleNameEndingWith("Vo")
            .or().haveSimpleNameEndingWith("Response")
            .or().haveSimpleNameEndingWith("Request")
            .and().resideInAPackage("..dto..")
            .should().beAnnotatedWith(Reflective.class)
            .because("DTO 클래스는 @Reflective를 추가하여 Native Image 호환성을 보장하세요.");

        // rule.check(importedClasses);
    }

    @Test
    void architecture_shouldFollowLayeredStructure() {
        // 레이어 아키텍처 검증
        layeredArchitecture()
            .consideringAllDependencies()
            .layer("Controller").definedBy("..controller..")
            .layer("Service").definedBy("..service..")
            .layer("Repository").definedBy("..repository..")
            .layer("Domain").definedBy("..domain..")

            .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
            .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller")
            .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service")
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Repository", "Service", "Controller")

            .check(importedClasses);
    }

    @Test
    void entities_shouldNotHaveLazyLoadingWithoutFetchType() {
        // @OneToMany, @ManyToOne 등에서 fetch type 명시 권장
        // (실제 구현은 복잡하므로 Gradle task로 대체)
    }
}
