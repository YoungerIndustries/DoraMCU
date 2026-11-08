# Stage 1: Build
FROM eclipse-temurin:27-jdk AS build
WORKDIR /app
COPY build.gradle settings.gradle gradlew ./
COPY gradle/wrapper gradle/wrapper
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

# DL_MODE: Set to "rest" to use the Python DL microservice (dora-dl-python) instead of local DL4J.
# When using REST mode, the Java app calls the Python service on port 8083 via HTTP.
# This eliminates the need for large JVM memory and swap for DL4J training.
# Default: "local" (uses DL4J in-process)
ENV DL_MODE=local

# Create swap file on disk for local DL4J mode.
# When DL_MODE=rest, swap is not needed (Python service handles ML separately).
# Override at build time: docker build --build-arg SWAP_SIZE=8G ...
ARG SWAP_SIZE=8G

# Create the swap file at build time using the ARG value
RUN dd if=/dev/zero of=/swapfile bs=1M count=$(( ${SWAP_SIZE%G} * 1024 )) \
    && chmod 600 /swapfile \
    && mkswap /swapfile

# JVM memory standardized to match build.gradle bootRun settings.
# Override at runtime: docker run -e JVM_XMX="-Xmx8G" ...
# When DL_MODE=rest, DL4J is not loaded — lower memory is sufficient.
# When DL_MODE=local, DL4J memory args are needed for neural network training.
ENTRYPOINT ["sh", "-c", "\
  # Activate swap file if not already active (only needed for local DL4J mode) \
  if [ \"$DL_MODE\" != \"rest\" ] && ! swapon --show | grep -q /swapfile; then \
    swapon /swapfile && echo 'Swap activated on disk'; \
  fi && \
  # Set DL REST client property based on DL_MODE \
  DL_ARGS=\"\" && \
  if [ \"$DL_MODE\" = \"rest\" ]; then \
    DL_ARGS=\"-Ddl.rest-client.enabled=true\"; \
  fi && \
  java ${JVM_XMX:--Xmx4G} -XX:+UseG1GC -XX:MaxGCPauseMillis=200 \
    -Dorg.bytedeco.javacpp.alwaysscan=true \
    -Dorg.bytedeco.javacpp.maxbytes=2G \
    -Dorg.bytedeco.javacpp.maxphysicalbytes=6G \
    $DL_ARGS \
    -jar app.jar"]
