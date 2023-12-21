FROM golang:1.21.3-bookworm as build
ENV NATIVE_IMAGES=""
ENV LDFLAGS=""
ENV CGO_ENABLED=0

WORKDIR /app
COPY go/. .
RUN go mod download
RUN go build -ldflags "$LDFLAGS" -o /native/$VERSION/esthesis-core-device-$platform-$architecture cmd/main.go

# gcr.io/distroless/static-debian12
#FROM gcr.io/distroless/static-debian12@sha256:22cda3953e236576e5d113fc6a9290b27e179fbe5937efb1fdc16d81ddfb6120
FROM busybox:1.36.1-musl
LABEL maintainer="esthesis@eurodyn.com"

COPY --chown=65532:65532 go/scripts/samples/provisioning-handlers/firmware-update.sh /app/
COPY --chown=65532:65532 go/scripts/samples/version/version /app/.esthesis/version
COPY --chown=65532:65532 --from=build /native/$VERSION/esthesis-core-device-$platform-$architecture /app/esthesis-core-device
EXPOSE 8080 1883

ENTRYPOINT ["/app/esthesis-core-device"]
