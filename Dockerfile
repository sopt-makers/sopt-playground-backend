FROM amazoncorretto:17
RUN yum update -y && \
    yum install -y ca-certificates && \
    update-ca-trust
WORKDIR /app
COPY ./build/libs/internal-0.0.1-SNAPSHOT.jar /app/APPLICATION.jar

ENV SPRING_PROFILES_ACTIVE=dev

CMD ["java", "-Duser.timezone=Asia/Seoul", "-jar", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "APPLICATION.jar"]
