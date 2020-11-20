FROM openjdk:13.0.2-jdk
MAINTAINER esthesis@eurodyn.com

# set working directory
WORKDIR /app

# add app
COPY target/esthesis-backend.jar /app

# Change entropy source and random blocking algorithm to non-blocking.
COPY docker/java.security /usr/lib/jvm/zulu-8/jre/lib/security

ENTRYPOINT ["/usr/bin/java"]
CMD ["-jar", "-Dspring.profiles.active=prod", "/app/esthesis-backend.jar"]
