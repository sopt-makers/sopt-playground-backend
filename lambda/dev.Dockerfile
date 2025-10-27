# GraalVM 네이티브 컴파일을 위한 멀티스테이지 빌드
FROM ghcr.io/graalvm/native-image-community:17.0.9 as builder

# 작업 디렉토리 설정
WORKDIR /app

# 필요한 빌드 도구 설치
RUN microdnf install -y findutils

# Gradle Wrapper와 빌드 설정 파일만 먼저 복사 (dependency 캐싱용)
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Gradle Wrapper 실행 권한 부여
RUN chmod +x ./gradlew

# Dependency 다운로드 (이 레이어는 build.gradle이 변경되지 않으면 캐시됨)
RUN ./gradlew dependencies --no-daemon || true

# 소스 코드 복사 (dependency 다운로드 후)
COPY src ./src

# 네이티브 컴파일 실행 (메모리 최적화)
ENV SPRING_PROFILES_ACTIVE=lambda-dev
ENV GRADLE_OPTS="-Xmx3g"
RUN ./gradlew clean nativeCompile -x test --no-daemon \
    -Dorg.gradle.jvmargs="-Xmx3g" \
    && ls -lah /app/build/native/nativeCompile/

# 최종 이미지 - glibc 호환을 위해 amazonlinux 사용
FROM public.ecr.aws/amazonlinux/amazonlinux:2023

# Lambda adapter 복사 (ECR 공식 이미지에서)
COPY --from=public.ecr.aws/awsguru/aws-lambda-adapter:0.9.1 /lambda-adapter /opt/extensions/lambda-adapter

WORKDIR /app

# 네이티브 바이너리 복사 (빌드 스테이지에서)
COPY --from=builder /app/build/native/nativeCompile/internal /app/internal

# 환경변수 설정
ENV SPRING_PROFILES_ACTIVE=lambda-dev
ENV PORT=8080
ENV AWS_LWA_READINESS_CHECK_PATH=/actuator/health
ENV AWS_LWA_READINESS_CHECK_PORT=8080

# 네이티브 바이너리 실행 권한 부여 및 실행
RUN chmod +x /app/internal
CMD ["/app/internal"]
