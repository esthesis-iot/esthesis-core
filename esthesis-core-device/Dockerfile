# Build stage.
FROM golang:1.21.3-bookworm as build
ARG LDFLAGS=""
ENV CGO_ENABLED=0

WORKDIR /app
COPY go/. .
RUN ls -asl /
RUN go mod download
RUN go build -ldflags "$LDFLAGS" -o /native/esthesis-core-device cmd/main.go

# Runtime stage.
FROM scratch
LABEL maintainer="esthesis@eurodyn.com"

COPY --from=build /usr/share/zoneinfo /usr/share/zoneinfo
COPY --from=build /etc/ssl/certs/ca-certificates.crt /etc/ssl/certs/
COPY --from=build /etc/passwd /etc/passwd
COPY --from=build /etc/group /etc/group
COPY --chown=65532:65532 go/scripts/samples/provisioning-handlers/firmware-update.sh /app/
COPY --chown=65532:65532 go/scripts/samples/version/version /app/.esthesis/version
COPY --chown=65532:65532 --from=build /native/esthesis-core-device /app/esthesis-core-device
EXPOSE 8080 1883

ENTRYPOINT ["/app/esthesis-core-device"]
