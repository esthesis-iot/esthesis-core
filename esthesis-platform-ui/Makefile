BUILD_TAG := $(shell mvn -f ../esthesis-platform-backend/pom.xml -q -Dexec.executable=echo -Dexec.args='$${project.version}' --non-recursive exec:exec)

all: build push

build:
	echo Building image with tag: $(BUILD_TAG)
	rm -rf dist
	docker build . -t esthesis/esthesis-platform-frontend-server:$(BUILD_TAG) -t esthesis/esthesis-platform-frontend-server

push:
	docker push esthesis/esthesis-platform-frontend-server:$(BUILD_TAG)
	docker push esthesis/esthesis-platform-frontend-server:latest
