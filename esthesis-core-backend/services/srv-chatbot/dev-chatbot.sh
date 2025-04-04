#!/usr/bin/env bash

# Starter script.
# Arguments:
#   $1: Additional profiles to activate, comma-separated (e.g. profile1,profile2,profile3).
#   		'dev' profile activates by default.

# Source local environment variables.
[ -e "local-env.sh" ] && source "local-env.sh"

# Call starter script
source ../../../_dev/dev-scripts/start-quarkus.sh \
	LAUNCH_FOLDER="$(pwd)/srv-chatbot-impl" \
	MVNW_DIR="$(pwd)/../.." \
	WEB_PORT="59190" \
	DEBUG_PORT="59191" \
	PROFILES="${1:-dev}${1:+,dev}" \
	OIDC="true"
