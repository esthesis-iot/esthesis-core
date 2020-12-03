BUILD_TAG := $(shell mvn -q -Dexec.executable=echo -Dexec.args='$${project.version}' --non-recursive exec:exec)

all: build push

build:
	echo Building image with tag: $(BUILD_TAG)
	mvn clean install -Pprod
	docker build . -t esthesis/esthesis-platform-backend-server:$(BUILD_TAG) -t esthesis/esthesis-platform-backend-server

push:
	docker push esthesis/esthesis-platform-backend-server:$(BUILD_TAG)
	docker push esthesis/esthesis-platform-backend-server:latest
