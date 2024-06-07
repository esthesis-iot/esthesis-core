#!/usr/bin/env bash

# Starter script.
# Arguments:
#   $1: Additional profiles to activate. 'dev' profile activates by default.

# Source local environment variables.
[ -e "local-env.sh" ] && source "local-env.sh"

# Call starter script
source ../../../_dev/dev-scripts/start-quarkus.sh \
	LAUNCH_FOLDER="$(pwd)/srv-infrastructure-impl" \
	MVNW_DIR="$(pwd)/../.." \
	WEB_PORT="59110" \
	DEBUG_PORT="59111" \
	PROFILES="${1:-dev}${1:+,dev}" \
	OIDC="true" \
	OIDC_CLIENT="true" \
	MONGODB="true"
