FROM azul/zulu-openjdk-alpine:8u212
MAINTAINER esthesis@eurodyn.com

# set working directory
WORKDIR /app

# add app
COPY target/esthesis-platform-server.jar /app

# Change entropy source and random blocking algorithm to non-blocking.
COPY docker/java.security /usr/lib/jvm/zulu-8/jre/lib/security

ENTRYPOINT ["/usr/bin/java"]
CMD ["-jar", "-Dspring.profiles.active=prod", "/app/esthesis-platform-server.jar"]
