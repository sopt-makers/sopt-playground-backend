FROM amazoncorretto:17
COPY --from=public.ecr.aws/awsguru/aws-lambda-adapter:0.7.0 /lambda-adapter /opt/extensions/lambda-adapter
WORKDIR /app
COPY ./build/libs/internal-0.0.1-SNAPSHOT.jar /app/APPLICATION.jar

ENV SPRING_PROFILES_ACTIVE=dev
ENV PORT=8080
ENV AWS_LWA_READINESS_CHECK_PATH=/actuator/health

CMD ["java", "-Duser.timezone=Asia/Seoul", "-jar", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "APPLICATION.jar"]
