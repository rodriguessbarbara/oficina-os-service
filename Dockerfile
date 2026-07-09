FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
RUN ./gradlew dependencies --no-daemon --quiet || true
COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Datadog APM – rastreabilidade distribuída (Fase 3)
RUN wget -O dd-java-agent.jar 'https://dtdg.co/latest-java-tracer'

COPY --from=builder /app/build/libs/*.jar app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENV DD_TRACE_ENABLED=false

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -javaagent:/app/dd-java-agent.jar -jar /app/app.jar"]