####################################################################################################
# Docker multistage build for esthesis device agent.
####################################################################################################
ARG MAVEN_IMAGE

####################################################################################################
# Cache Maven dependencies.
####################################################################################################
FROM $MAVEN_IMAGE as maven
MAINTAINER esthesis@eurodyn.com

# Set working directory.
WORKDIR /maven

# Copy application's pom.xml.
COPY esthesis-device/pom.xml esthesis-device/pom.xml

# Copy dependencies pom.xml.
COPY esthesis-server/pom.xml esthesis-server/pom.xml
COPY esthesis-server/esthesis-server-common/pom.xml esthesis-server/esthesis-server-common/pom.xml

# Download Maven dependencies.
RUN cd esthesis-server/esthesis-server-common && mvn de.qaware.maven:go-offline-maven-plugin:resolve-dependencies -Pprod
RUN cd esthesis-device && mvn de.qaware.maven:go-offline-maven-plugin:resolve-dependencies -Pprod

####################################################################################################
# Build application.
####################################################################################################
FROM maven:3.8.1-openjdk-15 as build
MAINTAINER esthesis@eurodyn.com

# Set working directory
WORKDIR /build

COPY .git .git
COPY esthesis-server/pom.xml esthesis-server/pom.xml
COPY esthesis-server/esthesis-server-common/pom.xml esthesis-server/esthesis-server-common/pom.xml
COPY esthesis-server/esthesis-server-common/src esthesis-server/esthesis-server-common/src
COPY esthesis-device/pom.xml esthesis-device/pom.xml
COPY esthesis-device/src esthesis-device/src
COPY --from=maven /root/.m2 /root/.m2

RUN cd esthesis-server && mvn -N clean install -Pprod
RUN cd esthesis-server/esthesis-server-common && mvn clean install -Pprod
RUN cd esthesis-device && mvn clean package -Pprod

####################################################################################################
# Create application image.
####################################################################################################
FROM adoptopenjdk:15.0.2_7-jre-hotspot
MAINTAINER esthesis@eurodyn.com

# Set working directory
WORKDIR /app

# Add app
COPY --from=build /build/esthesis-device/target/esthesis-device*.jar /app/esthesis-device.jar

RUN sed -i.bak \
    -e "s/securerandom.source=file:\/dev\/random/securerandom.source=file:\/dev\/urandom/g" \
    -e "s/securerandom.strongAlgorithms=NativePRNGBlocking/securerandom.strongAlgorithms=NativePRNG/g" \
    $JAVA_HOME/conf/security/java.security

ENTRYPOINT ["/opt/java/openjdk/bin/java"]
CMD ["-jar", "-Dspring.profiles.active=prod", "/app/esthesis-device.jar"]
