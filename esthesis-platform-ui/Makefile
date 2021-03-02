# Build file for esthesis platform frontend.
# You can use a custom Docker context for your builds, e.g.:
#		make build-arm64 context="--context default"

BUILD_TAG := $(shell mvn -f ../esthesis-platform-backend/pom.xml -q -Dexec.executable=echo -Dexec.args='$${project.version}' --non-recursive exec:exec)

_create-buildx:
	if docker $(context) buildx ls | grep -q esthesis_ui; then echo "esthesis_ui buildx already exists."; else docker $(context) buildx create --name esthesis_ui --use; fi

all: build-all push-all

build-all: build-amd64 build-arm64

build-amd64: _create-buildx
	echo Building image with tag: $(BUILD_TAG)
	rm -rf dist
	docker $(context) buildx build -f Dockerfile.amd64 --platform linux/amd64 . -t esthesis/esthesis-platform-ui:$(BUILD_TAG)-amd64 --load

build-arm64: _create-buildx
	echo Building image with tag: $(BUILD_TAG)
	rm -rf dist
	docker $(context) buildx build -f Dockerfile.arm64 --platform linux/arm64 . -t esthesis/esthesis-platform-ui:$(BUILD_TAG)-arm64 --load

push-all: push-arm64 push-amd64

push-amd64:
	docker $(context) push esthesis/esthesis-platform-ui:$(BUILD_TAG)-amd64

push-arm64:
	docker $(context) push esthesis/esthesis-platform-ui:$(BUILD_TAG)-arm64

clear:
	docker $(context) buildx rm esthesis_backend
