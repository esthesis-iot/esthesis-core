####################################################################################################
# Docker multistage build setup for the backend app.
####################################################################################################

####################################################################################################
# Cache Maven dependencies.
####################################################################################################
FROM maven:3.8.1-openjdk-15 as maven
MAINTAINER esthesis@eurodyn.com

# Set working directory.
WORKDIR /maven

# Copy application's pom.xml.
COPY esthesis-server/pom.xml esthesis-server/pom.xml
COPY esthesis-server/esthesis-server-common/pom.xml esthesis-server/esthesis-server-common/pom.xml
COPY esthesis-server/esthesis-server-impl/pom.xml esthesis-server/esthesis-server-impl/pom.xml

# Download Maven dependencies.
RUN cd esthesis-server && mvn de.qaware.maven:go-offline-maven-plugin:resolve-dependencies -Pprod

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

COPY esthesis-server/esthesis-server-impl/pom.xml esthesis-server/esthesis-server-impl/pom.xml
COPY esthesis-server/esthesis-server-impl/src esthesis-server/esthesis-server-impl/src

COPY --from=maven /root/.m2 /root/.m2

RUN cd esthesis-server && mvn clean package -Pprod
#RUN cd esthesis-server-impl && mvn spring-boot:repackage -Pprod

####################################################################################################
# Create application image.
####################################################################################################
FROM adoptopenjdk:15.0.2_7-jre-hotspot
MAINTAINER esthesis@eurodyn.com

# Set working directory
WORKDIR /app

# Add app
COPY --from=build /build/esthesis-server/esthesis-server-impl/target/esthesis-server-impl-*.jar /app/esthesis-server.jar

ENTRYPOINT ["/opt/java/openjdk/bin/java"]
CMD ["-jar", "-Dspring.profiles.active=prod", "/app/esthesis-server.jar"]
