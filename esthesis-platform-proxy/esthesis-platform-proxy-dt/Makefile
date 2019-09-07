BUILD_TAG := $(shell mvn -f ../../pom.xml -q -Dexec.executable=echo -Dexec.args='$${project.version}' --non-recursive exec:exec)

all: build push

build:
	echo Building image with tag: $(BUILD_TAG)
	rm -rf dist
	docker build . -t esthesis/esthesis-platform-proxy-dt:$(BUILD_TAG) -t esthesis/esthesis-platform-proxy-dt

push:
	docker push esthesis/esthesis-platform-proxy-dt:$(BUILD_TAG)
	docker push esthesis/esthesis-platform-proxy-dt:latest
