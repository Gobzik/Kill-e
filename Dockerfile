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
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle
COPY src ./src
RUN gradle clean build --no-daemon -x test

FROM bitnami/gradle:latest
USER root
RUN rm -rf /opt/bitnami/gradle
COPY --from=builder /opt/jdk-24 /opt/jdk-24
ENV JAVA_HOME=/opt/jdk-24
ENV PATH=$JAVA_HOME/bin:$PATH
WORKDIR /app
RUN groupadd -r appuser && useradd -r -g appuser appuser
COPY --from=builder /app/build/libs/*.jar app.jar
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]