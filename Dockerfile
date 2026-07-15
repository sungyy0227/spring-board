FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /workspace

COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew

COPY src src

RUN ./gradlew bootJar --no-daemon && \
    JAR_FILE=$(find build/libs -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' -print -quit) && \
    test -n "$JAR_FILE" && \
    cp "$JAR_FILE" /workspace/app.jar

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

RUN groupadd --system spring && \
    useradd --system --gid spring --home-dir /app --no-create-home spring && \
    mkdir -p /app/uploads/post-images && \
    chown -R spring:spring /app

COPY --from=builder --chown=spring:spring /workspace/app.jar /app/app.jar

ENV SPRING_PROFILES_ACTIVE=prod

USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
