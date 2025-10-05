FROM --platform=linux/arm64 amazoncorretto:17-alpine

# Lambda adapter 복사 (ECR 공식 이미지에서)
COPY --from=public.ecr.aws/awsguru/aws-lambda-adapter:0.9.1 /lambda-adapter /opt/extensions/lambda-adapter

WORKDIR /app
COPY ./build/libs/internal-0.0.1-SNAPSHOT.jar /app/APPLICATION.jar

# 환경변수 설정
ENV SPRING_PROFILES_ACTIVE=lambda-dev
ENV PORT=8080
ENV AWS_LWA_READINESS_CHECK_PATH=/actuator/health
ENV AWS_LWA_READINESS_CHECK_PORT=8080

CMD ["java", \
    "-XX:+TieredCompilation", \
    "-XX:TieredStopAtLevel=1", \
    "-XX:+UseSerialGC", \
    "-XX:MaxRAMPercentage=75", \
    "-XX:InitialRAMPercentage=75", \
    "-XX:+UseStringDeduplication", \
    "-XX:+UseCompressedOops", \
    "-Duser.timezone=Asia/Seoul", \
    "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", \
    "-Dspring.jmx.enabled=false", \
    "-Dspring.main.lazy-initialization=true", \
    "-Dspring.data.jpa.repositories.bootstrap-mode=lazy", \
    "-Dspring.jpa.defer-datasource-initialization=true", \
    "-Dspring.jpa.hibernate.ddl-auto=none", \
    "-jar", "APPLICATION.jar"]
