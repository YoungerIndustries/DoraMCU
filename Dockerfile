# Stage 1: Build
FROM eclipse-temurin:27-jdk AS build
WORKDIR /app
COPY build.gradle settings.gradle gradlew gradle.wrapper* ./
COPY gradle gradle
COPY src src
COPY dora-ai dora-ai
COPY dora-dl-core dora-dl-core
RUN chmod +x gradlew
RUN ./gradlew bootJar --no-daemon

# Stage 2: Run
FROM eclipse-temurin:27-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Xmx6G", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=200", "-jar", "app.jar"]
