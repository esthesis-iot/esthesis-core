FROM openjdk:13.0.2-jdk
MAINTAINER esthesis@eurodyn.com

# Set working directory.
WORKDIR /app

# Add app.
COPY target/esthesis-platform-device.jar /app

RUN sed -i.bak \
    -e "s/securerandom.source=file:\/dev\/random/securerandom.source=file:\/dev\/urandom/g" \
    -e "s/securerandom.strongAlgorithms=NativePRNGBlocking/securerandom.strongAlgorithms=NativePRNG/g" \
    $JAVA_HOME/conf/security/java.security

CMD ["/usr/bin/java", "-jar", "-Dspring.profiles.active=prod", "/app/esthesis-platform-device.jar"]
