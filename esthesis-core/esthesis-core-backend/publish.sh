#!/usr/bin/env bash

# Array with all modules to be published.
modules=(
	"dataflows/dfl-command-reply-updater"
	"dataflows/dfl-influxdb-writer"
	"dataflows/dfl-mqtt-client"
	"dataflows/dfl-orion-gateway"
	"dataflows/dfl-ping-updater"
	"dataflows/dfl-rdbms-writer"
	"dataflows/dfl-redis-cache"
	"services/srv-about/srv-about-impl"
	"services/srv-agent/srv-agent-impl"
	"services/srv-application/srv-application-impl"
	"services/srv-audit/srv-audit-impl"
	"services/srv-campaign/srv-campaign-impl"
	"services/srv-command/srv-command-impl"
	"services/srv-crypto/srv-crypto-impl"
	"services/srv-dataflow/srv-dataflow-impl"
	"services/srv-device/srv-device-impl"
	"services/srv-dt/srv-dt-impl"
	"services/srv-infrastructure/srv-infrastructure-impl"
	"services/srv-kubernetes/srv-kubernetes-impl"
	"services/srv-provisioning/srv-provisioning-impl"
	"services/srv-public-access/srv-public-access-impl"
	"services/srv-security/srv-security-impl"
	"services/srv-settings/srv-settings-impl"
	"services/srv-tag/srv-tag-impl"
)

# If the user has provided a specific module to be published, replace the modules.
if [ $# -eq 1 ]; then
	modules=("$1")
fi

# Clean and build the project.
mvn clean
mvn -T 1C install

# Iterate over all modules and publish them.
for module in "${modules[@]}"; do
	pushd . && \
	cd "$module" && \
	./mvnw -T 1C package \
  	-Dquarkus.bootstrap.workspace-discovery=true \
    -Dquarkus.container-image.build=true \
    -Dquarkus.container-image.push=true \
    -Dquarkus.container-image.additional-tags=latest \
    -Dquarkus.jib.platforms=linux/amd64,linux/arm64/v8
  popd || exit
done
