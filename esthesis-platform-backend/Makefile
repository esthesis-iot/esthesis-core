# Build file for esthesis platform backend.
# You can use a custom Docker context for your builds, e.g.:
#		make build-arm64 context="--context default"

BUILD_TAG := $(shell mvn -q -Dexec.executable=echo -Dexec.args='$${project.version}' --non-recursive exec:exec)

_create-buildx:
	if docker $(context) buildx ls | grep -q esthesis_backend; then echo "esthesis_backend buildx already exists."; else docker $(context) buildx create --name esthesis_backend --use; fi
all: build-all push-all

build-all: build-src build-amd64 build-arm64

build-src:
	echo "Building image with tag: $(BUILD_TAG)"
	mvn clean install -Pprod

build-amd64: _create-buildx
	docker $(context) buildx build -f Dockerfile.amd64 --platform linux/amd64 . -t esthesis/esthesis-platform-backend:$(BUILD_TAG)-amd64 --load

build-arm64: _create-buildx
	docker $(context) buildx build -f Dockerfile.arm64 --platform linux/arm64 . -t esthesis/esthesis-platform-backend:$(BUILD_TAG)-arm64 --load

push-all: push-amd64 push-arm64

push-amd64:
	docker $(context) push esthesis/esthesis-platform-backend:$(BUILD_TAG)-amd64

push-arm64:
	docker $(context) push esthesis/esthesis-platform-backend:$(BUILD_TAG)-arm64

clear:
	docker $(context) buildx rm esthesis_backend
