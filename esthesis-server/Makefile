# Build file for esthesis platform backend.

BUILD_TAG := $(shell mvn -q -Dexec.executable=echo -Dexec.args='$${project.version}' --non-recursive exec:exec)

all: build push

build:
	$(info Building image with tag: $(BUILD_TAG))
	docker build . -t esthesisiot/esthesis-server:latest -t esthesisiot/esthesis-server:$(BUILD_TAG)

push:
	docker push esthesisiot/server:$(BUILD_TAG)
	docker push esthesisiot/server:latest
