FROM bitnami/gradle:latest AS builder
USER root
RUN install_packages wget
RUN wget https://download.java.net/java/GA/jdk24/1f9ff9062db4449d8ca828c504ffae90/36/GPL/openjdk-24_linux-aarch64_bin.tar.gz && \
    tar -xzf openjdk-24_linux-aarch64_bin.tar.gz && \
    mv jdk-24 /opt/jdk-24 && \
    rm openjdk-24_linux-aarch64_bin.tar.gz
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