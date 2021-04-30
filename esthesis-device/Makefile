# Build file for esthesis Device Agent.
# You can use a custom Docker context for your builds, e.g.:
#		make build-arm64 context="--context default"

BUILD_TAG := $(shell mvn -f ./pom.xml -q -Dexec.executable=echo -Dexec.args='$${project.version}' --non-recursive exec:exec)

all: build-all push-all

build-all: build-src build-amd64 build-arm64 build-armv7

build-src:
	$(info Building image with tag: $(BUILD_TAG))
	mvn clean install -Pprod

build-amd64:
	docker $(context) build --platform linux/amd64 . -t esthesis/device:$(BUILD_TAG)-amd64

build-arm64:
	docker $(context) build --platform linux/arm64 . -t esthesis/device:$(BUILD_TAG)-arm64

build-armv7:
	docker $(context) build --platform linux/arm/v7 . -t esthesis/device:$(BUILD_TAG)-armv7

push-all: push-amd64 push-arm64 push-armv7 push-manifest

push-amd64:
	docker $(context) push esthesis/device:$(BUILD_TAG)-amd64

push-arm64:
	docker $(context) push esthesis/device:$(BUILD_TAG)-arm64

push-armv7:
	docker $(context) push esthesis/device:$(BUILD_TAG)-armv7


# Utility
# Build & push manifest
push-manifest:
	$(info Checking for existing manifest esthesis/device:$(BUILD_TAG))
	@docker manifest inspect esthesis/device:$(BUILD_TAG); \
	if [ $$? -eq 0 ]; then \
		 	docker $(context) manifest rm esthesis/device:$(BUILD_TAG) \
	;fi
	docker $(context) manifest create \
		esthesis/device:$(BUILD_TAG) \
		esthesis/device:$(BUILD_TAG)-amd64 \
		esthesis/device:$(BUILD_TAG)-arm64 \
		esthesis/device:$(BUILD_TAG)-armv7
	docker $(context) manifest push esthesis/device:$(BUILD_TAG)
