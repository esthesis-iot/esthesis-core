# Build file for esthesis Device Agent.
# You can use a custom Docker context for your builds, e.g.:
#		make build-arm64 context="--context default"

BUILD_TAG := $(shell mvn -f ./pom.xml -q -Dexec.executable=echo -Dexec.args='$${project.version}' --non-recursive exec:exec)

_create-buildx:
	if docker $(context) buildx ls | grep -q esthesis_device; then echo "esthesis_device buildx already exists."; else docker $(context) buildx create --name esthesis_device --use; fi

all: build-all push-all

build-all: build-src build-amd64 build-arm64 build-armv7

build-src:
	echo "Building image with tag: $(BUILD_TAG)"
	mvn clean install -Pprod

build-amd64: _create-buildx
	docker $(context) buildx build -f Dockerfile.amd64 --platform linux/amd64 . -t esthesis/esthesis-platform-device:$(BUILD_TAG)-amd64 --load

build-arm64: _create-buildx
	docker $(context) buildx build -f Dockerfile.arm64 --platform linux/arm64 . -t esthesis/esthesis-platform-device:$(BUILD_TAG)-arm64 --load

build-armv7: _create-buildx
	docker $(context) buildx build -f Dockerfile.armv7 --platform linux/arm/v7 . -t esthesis/esthesis-platform-device:$(BUILD_TAG)-armv7 --load

push-all: push-amd64 push-arm64 push-armv7

push-amd64:
	docker $(context) push esthesis/esthesis-platform-device:$(BUILD_TAG)-amd64

push-arm64:
	docker $(context) push esthesis/esthesis-platform-device:$(BUILD_TAG)-arm64

push-armv7:
	docker $(context) push esthesis/esthesis-platform-device:$(BUILD_TAG)-armv7

clear:
	docker $(context) buildx rm esthesis_device
