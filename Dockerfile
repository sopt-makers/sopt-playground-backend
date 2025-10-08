# GraalVM 네이티브 컴파일을 위한 멀티스테이지 빌드
FROM --platform=linux/arm64 ghcr.io/graalvm/graalvm-community:21.0.1 as builder

# 작업 디렉토리 설정
WORKDIR /app

# 소스 코드 복사
COPY . .

# Gradle Wrapper 실행 권한 부여
RUN chmod +x ./gradlew

# 네이티브 컴파일 실행
RUN ./gradlew clean nativeCompile -x test

# 최종 Lambda 이미지
FROM --platform=linux/arm64 amazoncorretto:17-alpine

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
