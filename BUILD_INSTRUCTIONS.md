# Native Image Build Instructions

## Docker Desktop Memory Configuration (필수)

Native Image 컴파일은 최소 **12GB** 메모리가 필요합니다.

### macOS Docker Desktop 설정:
1. Docker Desktop 앱 실행
2. **⚙️ Settings** → **Resources** → **Memory**
3. 메모리를 **12 GB** 이상으로 설정
4. **Apply & Restart** 클릭

### 메모리 확인:
```bash
docker info | grep -i memory
# Total Memory: 12GiB 이상이어야 함
```

## 빌드 및 배포

### 1. Docker 이미지 빌드:
```bash
docker build -t sopt-playground-backend:native .
```

빌드 시간: 약 15-20분 소요

### 2. ECR 배포:
```bash
./lambda/lambda_deploy.sh
```

## 빌드 중 나타나는 Warning 안내

빌드 중 다음과 같은 Warning들이 나타나지만 **정상**입니다:
- `Could not resolve class org.conscrypt.*` - 선택적 암호화 라이브러리 (미사용)
- `Could not resolve class com.github.luben.zstd.*` - 선택적 압축 라이브러리 (미사용)
- `Could not resolve class org.joda.time.*` - 레거시 날짜 라이브러리 (미사용)
- Netty, Groovy, Log4j 관련 경고 - Optional dependencies

이들은 Spring Boot와 의존 라이브러리들이 "있으면 사용" 하려고 체크하는 것들이며,
없어도 애플리케이션 동작에 **전혀 문제가 없습니다**.

## Feign Client Native Image 설정

`FeignNativeHints.java` 파일 하나로 모든 Feign 관련 reflection 설정이 자동으로 처리됩니다.
별도의 META-INF 설정 파일은 필요하지 않습니다.
