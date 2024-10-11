####################################################################################################
# esthesis device agent image builder, using 'scratch' as base image.
# This image produces a minimal image that contains only the esthesis-core-device agent and its
#
# dependencies. You do not have a shell, nor you can execute commands using the pseudo-terminal
# of esthesis CORE.
#
# The image is built in two stages:
# 1. Build stage: The esthesis-core-device agent is built using the golang:1.21.3-bookworm image.
# 2. Runtime stage: The esthesis-core-device agent is copied to a new image based on 'scratch'.
####################################################################################################

# Build stage.
FROM golang:1.21.3-bookworm AS build
ARG LDFLAGS=""
ENV CGO_ENABLED=0

WORKDIR /app
COPY go/. .
RUN go mod download
RUN go build -ldflags "$LDFLAGS" -o /native/esthesis-core-device cmd/main.go
RUN mkdir /nonexistent

# Runtime stage.
FROM scratch
LABEL maintainer="esthesis@eurodyn.com"

COPY --from=build /usr/share/zoneinfo /usr/share/zoneinfo
COPY --from=build /etc/ssl/certs/ca-certificates.crt /etc/ssl/certs/
COPY --from=build /etc/passwd /etc/passwd
COPY --from=build /etc/group /etc/group
COPY --chown=65534:65534 go/scripts/samples/provisioning-handlers/firmware-update.sh /app/
COPY --chown=65534:65534 go/scripts/samples/version/version /app/.esthesis/version
COPY --chown=65534:65534 --from=build /native/esthesis-core-device /app/esthesis-core-device
COPY --chown=65534:65534 --from=build /nonexistent /nonexistent

EXPOSE 8080 1883
USER nobody
ENTRYPOINT ["/app/esthesis-core-device"]
