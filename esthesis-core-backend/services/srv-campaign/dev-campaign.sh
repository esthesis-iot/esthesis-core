#!/usr/bin/env bash

# Starter script.
# Arguments:
#   $1: Additional profiles to activate. 'dev' profile activates by default.

# Source local environment variables.
[ -e "local-env.sh" ] && source "local-env.sh"

# Call starter script
source ../../../_dev/dev-scripts/start-quarkus.sh \
	LAUNCH_FOLDER="$(pwd)/srv-campaign-impl" \
	MVNW_DIR="$(pwd)/../.." \
	WEB_PORT="59150" \
	DEBUG_PORT="59151" \
	PROFILES="${1:-dev}${1:+,dev}" \
	OIDC="true" \
	OIDC_CLIENT="true" \
	MONGODB="true" \
	ZEEBE="true" \
	REDIS="true"

