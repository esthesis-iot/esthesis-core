####################################################################################################
# Docker multistage build for esthesis server backend.
# Build this Dockerfile from `esthesis-platform/` by executing:
# `docker compose build esthesis-server`.
####################################################################################################

####################################################################################################
# Cache Maven dependencies.
####################################################################################################
FROM maven:3.8.1-openjdk-15 as maven
LABEL maintainer="esthesis@eurodyn.com"

# Set working directory.
WORKDIR /maven

# Copy application's pom.xml.
COPY esthesis-server/pom.xml pom.xml
COPY esthesis-server/esthesis-server-common/pom.xml esthesis-server-common/pom.xml
COPY esthesis-server/esthesis-server-impl/pom.xml esthesis-server-impl/pom.xml

# Download Maven dependencies.
RUN mvn de.qaware.maven:go-offline-maven-plugin:resolve-dependencies -Pprod

####################################################################################################
## Build application.
####################################################################################################
FROM maven:3.8.1-openjdk-15 as build
LABEL maintainer="esthesis@eurodyn.com"

# Set working directory
WORKDIR /build

COPY .git .git
COPY esthesis-server/pom.xml pom.xml
COPY esthesis-server/esthesis-server-common/pom.xml esthesis-server-common/pom.xml
COPY esthesis-server/esthesis-server-common/src esthesis-server-common/src
COPY esthesis-server/esthesis-server-impl/pom.xml esthesis-server-impl/pom.xml
COPY esthesis-server/esthesis-server-impl/src esthesis-server-impl/src

COPY --from=maven /root/.m2 /root/.m2

RUN mvn clean package -Pprod

####################################################################################################
# Create application image.
####################################################################################################
FROM adoptopenjdk:15.0.2_7-jre-hotspot
LABEL maintainer="esthesis@eurodyn.com"

# Set working directory
WORKDIR /app

# Add app
COPY --from=build /build/esthesis-server-impl/target/esthesis-server-impl-*.jar /app/esthesis-server.jar

RUN sed -i.bak \
    -e "s/securerandom.source=file:\/dev\/random/securerandom.source=file:\/dev\/urandom/g" \
    -e "s/securerandom.strongAlgorithms=NativePRNGBlocking/securerandom.strongAlgorithms=NativePRNG/g" \
    $JAVA_HOME/conf/security/java.security

ENTRYPOINT ["/opt/java/openjdk/bin/java"]
CMD ["-jar", "-Dspring.profiles.active=prod", "/app/esthesis-server.jar"]
