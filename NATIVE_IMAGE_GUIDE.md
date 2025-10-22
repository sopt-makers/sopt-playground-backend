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

#### Rule 3: @Reflective는 QueryDSL Projection 사용 DTO에만 추가

**왜 @Reflective가 필요한가?**

Native Image는 **빌드 타임에 사용되는 모든 클래스를 분석**하여 바이너리에 포함시킵니다 (Closed World Assumption).
런타임에 리플렉션으로 클래스를 생성하려면 **빌드 타임에 미리 알려줘야** 합니다.

```java
// ✅ @Reflective 필요: QueryDSL Projections.constructor() 사용
@Reflective
public record ProjectDao(
    String name,
    String[] serviceType,  // 복잡한 타입 (배열, List 등)
    Long memberId
) {}

// Repository에서 사용
Projections.constructor(ProjectDao.class, project.name, project.serviceType, member.id)
// ↑ 런타임에 리플렉션으로 생성자 호출 → @Reflective 필수!
```

```java
// ❌ @Reflective 불필요: 일반 Request/Response (Jackson 직렬화)
public record LoginRequest(String email, String password) {}
public record LoginResponse(String accessToken) {}
// ↑ Spring Boot AOT가 Controller 스캔 시 자동으로 감지
```

**📌 적용 기준**

| DTO 종류 | @Reflective | 이유 |
|---------|-------------|------|
| **Dao** (QueryDSL 결과) | ✅ **필수** | `Projections.constructor()` 사용 |
| **Vo** (QueryDSL 결과) | ✅ **필수** | `Projections.constructor()` 사용 |
| **Request** (Controller 입력) | ❌ 불필요 | Spring Boot AOT 자동 감지 |
| **Response** (Controller 출력) | ❌ 불필요 | Spring Boot AOT 자동 감지 |

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
# build.gradle 설정 참고
./gradlew checkNativeImageCompatibility
```

### 실행 시 자동 검증
```bash
# 전체 빌드
./gradlew nativeCompile
```

---

## 📋 체크리스트

새로운 기능 추가 시:

- [ ] QueryDSL Projection은 `Projections.constructor()` 사용
- [ ] **QueryDSL 사용하는 Dao/Vo**에만 `@Reflective` 추가 (Request/Response는 불필요)
- [ ] Lazy Loading이 필요하면 EAGER 또는 Fetch Join
- [ ] 외부 라이브러리 사용 시 Native Image 호환성 확인

---

## 🚨 트러블슈팅

### 에러: "No constructor found"
→ QueryDSL 사용하는 **Dao/Vo**에 `@Reflective` 추가
→ Repository에서 `Projections.constructor()` 사용 확인

### 에러: "could not initialize proxy"
→ `FetchType.EAGER` 또는 Fetch Join 사용

### 에러: "ClassNotFoundException"
→ 외부 라이브러리 사용 시 `build.gradle`의 RuntimeHints에 추가

---

## 📚 참고 자료

- [Spring Boot Native Image 공식 문서](https://docs.spring.io/spring-boot/docs/current/reference/html/native-image.html)
- [GraalVM Native Image 가이드](https://www.graalvm.org/latest/reference-manual/native-image/)
- [Spring Boot AOT 컴파일: 성능 최적화의 새로운 패러다임](https://digitalbourgeois.tistory.com/324)
