# Build file for esthesis platform frontend.
# You can use a custom Docker context for your builds, e.g.:
#		make build-arm64 context="--context default"

BUILD_TAG := $(shell mvn -f ../esthesis-server/pom.xml -q -Dexec.executable=echo -Dexec.args='$${project.version}' --non-recursive exec:exec)

all: build-all push-all

build-all: build-amd64 build-arm64

build-amd64:
	echo Building image with tag: $(BUILD_TAG)
	rm -rf dist
	docker $(context) build --platform linux/amd64 . -t esthesis/ui:$(BUILD_TAG)-amd64

build-arm64:
	echo Building image with tag: $(BUILD_TAG)
	rm -rf dist
	docker $(context) build --platform linux/arm64 . -t esthesis/ui:$(BUILD_TAG)-arm64

push-all: push-arm64 push-amd64 push-manifest

push-amd64:
	docker $(context) push esthesis/ui:$(BUILD_TAG)-amd64

push-arm64:
	docker $(context) push esthesis/ui:$(BUILD_TAG)-arm64

# Utility
# Build & push manifest
push-manifest:
	$(info Checking for existing manifest esthesis/ui:$(BUILD_TAG))
	@docker manifest inspect esthesis/ui:$(BUILD_TAG); \
	if [ $$? -eq 0 ]; then \
		 	docker $(context) manifest create \
				--amend esthesis/ui:$(BUILD_TAG) \
				--amend esthesis/ui:$(BUILD_TAG)-amd64 \
				--amend esthesis/ui:$(BUILD_TAG)-arm64 \
	;else \
		docker $(context) manifest create \
				esthesis/ui:$(BUILD_TAG) \
				esthesis/ui:$(BUILD_TAG)-amd64 \
				esthesis/ui:$(BUILD_TAG)-arm64 \
	;fi
	docker $(context) manifest push esthesis/ui:$(BUILD_TAG)
