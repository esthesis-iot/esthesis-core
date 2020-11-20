BUILD_TAG := $(shell mvn -f ../pom.xml -q -Dexec.executable=echo -Dexec.args='$${project.version}' --non-recursive exec:exec)

all: build push

build:
	echo Building image with tag: $(BUILD_TAG)
	rm -rf dist
	docker build . -t esthesis/esthesis-ui:$(BUILD_TAG) -t esthesis/esthesis-ui

push:
	docker push esthesis/esthesis-ui:$(BUILD_TAG)
	docker push esthesis/esthesis-ui:latest
