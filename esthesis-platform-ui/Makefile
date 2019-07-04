BUILD_TAG := $(shell mvn -f ../pom.xml -q -Dexec.executable=echo -Dexec.args='$${project.version}' --non-recursive exec:exec)

all: build push

build:
	echo Building image with tag: $(BUILD_TAG)
	rm -rf dist
	docker build . -t esthesis/platform-ui:$(BUILD_TAG)
	docker build . -t esthesis/platform-ui:latest

push:
	docker login
	docker push esthesis/platform-ui:$(BUILD_TAG)
	docker push esthesis/platform-ui:latest

