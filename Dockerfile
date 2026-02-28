# bitnami/gradle:latest as of 2026-02-24
FROM bitnami/gradle:latest@sha256:64d73355296cbbe3c8e167ebdab985e9856fcc7717ea9f0ed915bed7ba7c1ebc AS builder
ARG TARGETARCH
USER root
RUN install_packages wget
RUN set -e; \
    case "$TARGETARCH" in \
      "amd64") JDK_ARCH="x64"; JDK_SHA256="88b090fa80c6c1d084ec9a755233967458788e2c0777ae2e172230c5c692d7ef" ;; \
      "arm64") JDK_ARCH="aarch64"; JDK_SHA256="a03867ed061c7bb661231e62b0967ff5a5a0b1bbaa37bdead3a924bd2ba3215f" ;; \
      *) echo "Unsupported TARGETARCH: $TARGETARCH" >&2; exit 1 ;; \
    esac; \
    JDK_TARBALL="openjdk-24_linux-${JDK_ARCH}_bin.tar.gz"; \
    wget "https://download.java.net/java/GA/jdk24/1f9ff9062db4449d8ca828c504ffae90/36/GPL/${JDK_TARBALL}" && \
    echo "${JDK_SHA256}  ${JDK_TARBALL}" | sha256sum -c - && \
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

# eclipse-temurin:24-jre as of 2026-02-28
FROM eclipse-temurin:24-jre@sha256:8cb2387a28af84cf0db0948d9c67d4480192f4e567027a3963f145d218e8b4f2
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
