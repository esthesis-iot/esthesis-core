BUILD_TAG := $(shell mvn -f ../pom.xml -q -Dexec.executable=echo -Dexec.args='$${project.version}' --non-recursive exec:exec)

all: build push

build:
	echo Building image with tag: $(BUILD_TAG)
	mvn clean install -DskipTests=true -f ../pom.xml
	mvn clean install spring-boot:repackage -Pprod
	docker build . -t esthesis/esthesis-backend:$(BUILD_TAG) -t esthesis/esthesis-backend

push:
	docker push esthesis/esthesis-ui:$(BUILD_TAG)
	docker push esthesis/esthesis-ui:latest
