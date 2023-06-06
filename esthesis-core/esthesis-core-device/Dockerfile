FROM golang:1.20.4-bullseye as build

WORKDIR /app
COPY go/. .
RUN go mod download
RUN go build -o esthesis-core-device -ldflags '-linkmode external -w -extldflags "-static"' cmd/main.go
#RUN go build -o esthesis-agent cmd/main.go

FROM alpine:3.18.0
COPY --from=build /app/esthesis-core-device /app/esthesis-core-device
EXPOSE 8080 1883

# Specifies the executable command that runs when the container starts
CMD ["/app/esthesis-core-device"]
