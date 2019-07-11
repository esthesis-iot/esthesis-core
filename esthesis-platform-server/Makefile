BUILD_TAG := $(shell mvn -f ../pom.xml -q -Dexec.executable=echo -Dexec.args='$${project.version}' --non-recursive exec:exec)

build:
	echo Building image with tag: $(BUILD_TAG)
	mvn clean install -f ../../esthesis-bom/pom.xml
	mvn clean install -f ../../esthesis-parent/pom.xml
	mvn clean install spring-boot:repackage -DskipTests=true
	mv target/esthesis-platform-server-$(BUILD_TAG).jar target/esthesis-platform-server.jar
	docker build . -t esthesis/platform-server:$(BUILD_TAG)
	docker build . -t esthesis/platform-server:latest

push_ed:
	docker tag esthesis/platform-server:$(BUILD_TAG) dreg.eurodyn.com/esthesis-platform-server:$(BUILD_TAG)
	docker push dreg.eurodyn.com/esthesis-platform-server:$(BUILD_TAG)
	docker tag esthesis/platform-server:latest dreg.eurodyn.com/esthesis-platform-server:latest
	docker push dreg.eurodyn.com/esthesis-platform-server:latest
