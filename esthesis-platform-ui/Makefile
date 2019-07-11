BUILD_TAG := $(shell mvn -f ../pom.xml -q -Dexec.executable=echo -Dexec.args='$${project.version}' --non-recursive exec:exec)

all: build

build:
	echo Building image with tag: $(BUILD_TAG)
	rm -rf dist
	docker build . -t esthesis/platform-ui:$(BUILD_TAG)
	docker build . -t esthesis/platform-ui:latest

push_ed:
	docker tag esthesis/platform-ui:$(BUILD_TAG) dreg.eurodyn.com/esthesis-platform-ui:$(BUILD_TAG)
	docker push dreg.eurodyn.com/esthesis-platform-ui:$(BUILD_TAG)
	docker tag esthesis/platform-ui:latest dreg.eurodyn.com/esthesis-platform-ui:latest
	docker push dreg.eurodyn.com/esthesis-platform-ui:latest
