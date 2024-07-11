#!/usr/bin/env bash

# Starter script.

# Source local environment variables.
[ -e "local-env.sh" ] && source "local-env.sh"

# Start angular serve.
[ -n "$(command -v nvm)" ] && nvm use; npm start
