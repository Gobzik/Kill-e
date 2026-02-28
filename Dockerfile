FROM bitnami/gradle:latest AS builder
ARG TARGETARCH
USER root
RUN install_packages wget
RUN set -e; \
    case "$TARGETARCH" in \
      "amd64") JDK_ARCH="x64" ;; \
      "arm64") JDK_ARCH="aarch64" ;; \
      *) echo "Unsupported TARGETARCH: $TARGETARCH" >&2; exit 1 ;; \
    esac; \
    JDK_TARBALL="openjdk-24_linux-${JDK_ARCH}_bin.tar.gz"; \
    wget "https://download.java.net/java/GA/jdk24/1f9ff9062db4449d8ca828c504ffae90/36/GPL/${JDK_TARBALL}" && \
    tar -xzf "${JDK_TARBALL}" && \
    mv jdk-24 /opt/jdk-24 && \
    rm "${JDK_TARBALL}"
ENV JAVA_HOME=/opt/jdk-24
ENV PATH=$JAVA_HOME/bin:$PATH
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradle ./gradle
COPY src ./src
RUN chmod +x ./gradlew && ./gradlew clean build --no-daemon -x test

FROM eclipse-temurin:24-jre
USER root
WORKDIR /app
RUN groupadd -r appuser && useradd -r -g appuser appuser
COPY --from=builder /app/build/libs /app/build/libs
RUN set -e; \
    JAR_FILES=$(ls /app/build/libs/*.jar 2>/dev/null | grep -v '\-plain\.jar' || true); \
    if [ -z "$JAR_FILES" ]; then echo "No non-plain JAR found in /app/build/libs"; exit 1; fi; \
    if [ "$(echo "$JAR_FILES" | wc -l)" -ne 1 ]; then echo "Expected exactly one non-plain JAR, found:"; echo "$JAR_FILES"; exit 1; fi; \
    cp "$JAR_FILES" /app/app.jar
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]