BUILD_TAG := $(shell mvn -f ../pom.xml -q -Dexec.executable=echo -Dexec.args='$${project.version}' --non-recursive exec:exec)

# Check that given variables are set and all have non-empty values,
# die with an error otherwise.
#
# Params:
#   1. Variable name(s) to test.
#   2. (optional) Error message to print.
check_defined = \
    $(strip $(foreach 1,$1, \
        $(call __check_defined,$1,$(strip $(value 2)))))
__check_defined = \
    $(if $(value $1),, \
      $(error Undefined $1$(if $2, ($2))))

build:
	echo Building image with tag: $(BUILD_TAG)
	rm -rf dist
	docker build . -t esthesis/platform-ui:$(BUILD_TAG)
	docker build . -t esthesis/platform-ui:latest

push_ed: REGISTRY=dreg.eurodyn.com
push_ed: push_registry

push_registry:
	@:$(call check_defined, REGISTRY, "You need to set the target Docker Registry first.")
	docker tag esthesis/platform-ui:$(BUILD_TAG) $(REGISTRY)/esthesis-platform-ui:$(BUILD_TAG)
	docker push $(REGISTRY)/esthesis-platform-ui:$(BUILD_TAG)
	docker tag esthesis/platform-ui:latest $(REGISTRY)/esthesis-platform-ui:latest
	docker push $(REGISTRY)/esthesis-platform-ui:latest
