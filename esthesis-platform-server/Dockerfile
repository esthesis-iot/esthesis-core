FROM azul/zulu-openjdk-alpine:8u212
MAINTAINER esthesis@eurodyn.com

# set working directory
WORKDIR /app

# add app
COPY target/esthesis-platform-server.jar /app

ENTRYPOINT ["/usr/bin/java"]
CMD ["-jar", "/app/esthesis-platform-server.jar"]
