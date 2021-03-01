BUILD_TAG := $(shell mvn -f ../esthesis-platform-backend/pom.xml -q -Dexec.executable=echo -Dexec.args='$${project.version}' --non-recursive exec:exec)

#all: build push

build-amd64:
	echo Building image with tag: $(BUILD_TAG)
	rm -rf dist
	if docker buildx ls | grep -q esthesis_ui; then echo "esthesis_ui buildx already exists."; else docker buildx create --name esthesis_ui --use; fi
	docker buildx build -f Dockerfile.amd64 --platform linux/amd64 . -t esthesis/esthesis-platform-ui:$(BUILD_TAG)-amd64 --load

build-arm64:
	echo Building image with tag: $(BUILD_TAG)
	rm -rf dist
	if docker buildx ls | grep -q esthesis_ui; then echo "esthesis_ui buildx already exists."; else docker buildx create --name esthesis_ui --use; fi
	docker buildx build -f Dockerfile.arm64 --platform linux/arm64 . -t esthesis/esthesis-platform-ui:$(BUILD_TAG)-arm64 --load

push:
	docker push esthesis/esthesis-platform-frontend-server:$(BUILD_TAG)
	docker push esthesis/esthesis-platform-frontend-server:latest

clear:
	docker buildx rm esthesis_backend
