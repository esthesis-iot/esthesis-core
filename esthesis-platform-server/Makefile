BUILD_TAG := $(shell mvn -f ../pom.xml -q -Dexec.executable=echo -Dexec.args='$${project.version}' --non-recursive exec:exec)

all: build push

build:
	echo Building image with tag: $(BUILD_TAG)
	mvn clean install -f ../../esthesis-bom/pom.xml
	mvn clean install -f ../../esthesis-parent/pom.xml
	mvn clean install spring-boot:repackage -DskipTests=true
	mv target/esthesis-platform-server*.jar target/esthesis-platform-server.jar
	docker build . -t esthesis/platform-server:$(BUILD_TAG)
	docker build . -t esthesis/platform-server:latest

push:
	docker login
	docker push esthesis/platform-server:$(BUILD_TAG)
	docker push esthesis/platform-server:latest

