# Build file for esthesis platform backend.
# You can use a custom Docker context for your builds, e.g.:
#		make build-arm64 context="--context default"

BUILD_TAG := $(shell mvn -q -Dexec.executable=echo -Dexec.args='$${project.version}' --non-recursive exec:exec)

all: build-all push-all

build-all: build-src build-amd64 build-arm64

build-src:
	$(info Building image with tag: $(BUILD_TAG))
	mvn clean install -Pprod

build-amd64:
	docker $(context) build --platform linux/amd64 . -t esthesis/server:$(BUILD_TAG)-amd64

build-arm64:
	docker $(context) build --platform linux/arm64 . -t esthesis/server:$(BUILD_TAG)-arm64

push-all: push-amd64 push-arm64 push-manifest

push-amd64:
	docker $(context) push esthesis/server:$(BUILD_TAG)-amd64

push-arm64:
	docker $(context) push esthesis/server:$(BUILD_TAG)-arm64

# Utility
# Build & push manifest
push-manifest:
	$(info Checking for existing manifest esthesis/server:$(BUILD_TAG))
	@docker manifest inspect esthesis/server:$(BUILD_TAG); \
	if [ $$? -eq 0 ]; then \
		 	docker $(context) manifest create \
				--amend esthesis/server:$(BUILD_TAG) \
				--amend esthesis/server:$(BUILD_TAG)-amd64 \
				--amend esthesis/server:$(BUILD_TAG)-arm64 \
	;else \
		docker $(context) manifest create \
				esthesis/server:$(BUILD_TAG) \
				esthesis/server:$(BUILD_TAG)-amd64 \
				esthesis/server:$(BUILD_TAG)-arm64 \
	;fi
	docker $(context) manifest push esthesis/server:$(BUILD_TAG)
