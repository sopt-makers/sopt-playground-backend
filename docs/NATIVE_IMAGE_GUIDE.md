# Native Image 개발 가이드

## 🎯 자동화 전략

### 1. 자동으로 처리되는 것들 (Spring Boot AOT)
다음은 **코드 변경 없이** 자동으로 Native Image에 포함됩니다:

✅ `@Entity` 클래스
✅ `@Component`, `@Service`, `@Repository`, `@Controller`
✅ `@Configuration` 클래스
✅ Spring Bean의 모든 메서드/필드
✅ `@ConfigurationProperties`
✅ Hibernate Enhancement (build.gradle 설정 시)

### 2. 규칙 기반으로 처리 (수동 작업 최소화)

#### Rule 1: QueryDSL Projection은 Record + Projections.constructor() 사용
```java
// ✅ 권장 (자동 처리)
@Reflective
public record MemberDao(Long id, String name) {}

// Repository에서
Projections.constructor(MemberDao.class, member.id, member.name)
```

```java
// ❌ 비권장 (수동 RuntimeHints 필요)
@QueryProjection  // Q클래스 생성, primitive type 문제
public record MemberDao(Long id, String name) {}
```

#### Rule 2: Lazy Loading은 가능한 EAGER 또는 Fetch Join 사용
```java
// ✅ Option 1: EAGER (단순한 경우)
@OneToMany(fetch = FetchType.EAGER)
private List<Item> items;

// ✅ Option 2: Fetch Join (복잡한 경우)
@Query("SELECT m FROM Member m JOIN FETCH m.items WHERE m.id = :id")
Member findByIdWithItems(Long id);
```

```java
// ❌ LAZY + Session 외부 접근 = 에러
@OneToMany  // 기본값 LAZY
private List<Item> items;
```

#### Rule 3: 모든 DTO에 @Reflective 추가
```java
@Reflective  // ← 이거 하나면 자동!
public record UserResponse(Long id, String name) {}
```

### 3. 프로젝트별 RuntimeHints 템플릿

**새 프로젝트 시작 시:**
1. `NativeImageRuntimeHintsRegistrar.java` 복사
2. 패키지명만 변경
3. 프로젝트별 특수 케이스만 추가

```java
// 프로젝트별 특수 케이스만 여기에 추가
private void registerProjectSpecificTypes(RuntimeHints hints) {
    // 예: 외부 라이브러리가 리플렉션 사용하는 경우
}
```

---

## 🔧 자동화 도구

### Gradle Task: Native Image 빌드 전 체크
```bash
./gradlew checkNativeImageCompatibility
```

### 실행 시 자동 검증
```bash
# 로컬에서 빠르게 테스트
./gradlew nativeTest

# 전체 빌드
./gradlew nativeCompile
```

---

## 📋 체크리스트

새로운 기능 추가 시:

- [ ] QueryDSL Projection은 `Projections.constructor()` 사용
- [ ] DTO에 `@Reflective` 추가
- [ ] Lazy Loading이 필요하면 EAGER 또는 Fetch Join
- [ ] 외부 라이브러리 사용 시 Native Image 호환성 확인

---

## 🚨 트러블슈팅

### 에러: "No constructor found"
→ DTO에 `@Reflective` 추가 또는 `Projections.constructor()` 사용

### 에러: "could not initialize proxy"
→ `FetchType.EAGER` 또는 Fetch Join 사용

### 에러: "ClassNotFoundException"
→ `NativeImageRuntimeHintsRegistrar`에 클래스 추가

---

## 📚 참고 자료

- [Spring Boot Native Image 공식 문서](https://docs.spring.io/spring-boot/docs/current/reference/html/native-image.html)
- [GraalVM Native Image 가이드](https://www.graalvm.org/latest/reference-manual/native-image/)
