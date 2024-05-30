#!/usr/bin/env bash

# Generic service starter.
# Arguments:
#	 	$1: 'impl' folder to cd into.
#	 	$2: Port to run the web server on.
#	 	$3: Debug port to run the service on.
#   $4: Additional profiles to activate. 'dev' profile activates by default.

# Set profiles.
PROFILES="dev"
if [ "$4" != "" ]; then
  PROFILES="$PROFILES,$4"
fi
echo "Activating profiles: $PROFILES"

# Check if Quarkus console should be enabled.
CONSOLE=true
if [ "$TERM_PROGRAM" = tmux ]; then
  CONSOLE=false
  echo "Disabling Quarkus console in tmux."
fi

# Set environment variables.
NAMESPACE=$(kubens -c)
if [ "$NAMESPACE" = "" ]; then
	echo "***ERROR: Could not find current namespace. Exiting."
	exit 1
fi
ESTHESIS_KEYCLOAK_PUBLIC_KEY_URL="http://keycloak.$NAMESPACE/realms/esthesis/protocol/openid-connect/certs"
ESTHESIS_KEYCLOAK_AUTH_SERVER_URL="http://keycloak.$NAMESPACE/realms/esthesis"
ESTHESIS_MONGODB_URL="mongodb://mongodb-headless.$NAMESPACE:27017"
ESTHESIS_GRAFANA_TEMPO_DISTRIBUTOR_URL="http://grafana-tempo-distributor.$NAMESPACE:4317"
ESTHESIS_REDIS_URL="redis://:esthesis-system@redis-master.$NAMESPACE:6379/0"
ESTHESIS_KAFKA_URL="kafka.$NAMESPACE:9092"
ESTHESIS_CAMUNDA_ZEEBE_URL="camunda-zeebe-gateway.$NAMESPACE:26500"
echo "Using Keycloak Public Key URL: $ESTHESIS_KEYCLOAK_PUBLIC_KEY_URL"
echo "Using Keycloak Auth Server URL: $ESTHESIS_KEYCLOAK_AUTH_SERVER_URL"
echo "Using MongoDB URL: $ESTHESIS_MONGODB_URL"
echo "Using Grafana Tempo Distributor URL: $ESTHESIS_GRAFANA_TEMPO_DISTRIBUTOR_URL"
echo "Using Redis URL: $ESTHESIS_REDIS_URL"
echo "Using Kafka URL: $ESTHESIS_KAFKA_URL"

# Run Quarkus.
if [ "$2" != "0" ]; then
	echo "Running Quarkus on port $2."
	echo "Debugging Quarkus on port $3."
	cd "$1" || exit
		./mvnw quarkus:dev \
			-Dquarkus.http.port=$2 \
			-Ddebug=$3 \
			-Dquarkus.profile="$PROFILES" \
			-Dquarkus.console.enabled="$CONSOLE" \
			-Dmp.jwt.verify.publickey.location="$ESTHESIS_KEYCLOAK_PUBLIC_KEY_URL" \
			-Dquarkus.mongodb.connection-string="$ESTHESIS_MONGODB_URL" \
			-Dquarkus.otel.exporter.otlp.traces.endpoint="$ESTHESIS_GRAFANA_TEMPO_DISTRIBUTOR_URL" \
			-Dquarkus.redis.hosts="$ESTHESIS_REDIS_URL" \
			-Dkafka.bootstrap.servers="$ESTHESIS_KAFKA_URL" \
			-Dquarkus.zeebe.client.broker.gateway-address="$ESTHESIS_CAMUNDA_ZEEBE_URL" \
			-Dquarkus.oidc-client.auth-server-url="$ESTHESIS_KEYCLOAK_AUTH_SERVER_URL" \
			-Desthesis.oidc.authority="$ESTHESIS_KEYCLOAK_AUTH_SERVER_URL"
else
	echo "Debugging Quarkus on port $3."
	cd "$1" || exit
	./mvnw quarkus:dev \
		-Ddebug=$3 \
		-Dquarkus.profile="$PROFILES" \
		-Dquarkus.console.enabled="$CONSOLE" \
		-Dmp.jwt.verify.publickey.location="$ESTHESIS_KEYCLOAK_PUBLIC_KEY_URL" \
		-Dquarkus.mongodb.connection-string="$ESTHESIS_MONGODB_URL" \
		-Dquarkus.otel.exporter.otlp.traces.endpoint="$ESTHESIS_GRAFANA_TEMPO_DISTRIBUTOR_URL" \
		-Dquarkus.redis.hosts="$ESTHESIS_REDIS_URL" \
		-Dkafka.bootstrap.servers="$ESTHESIS_KAFKA_URL" \
		-Dquarkus.zeebe.client.broker.gateway-address="$ESTHESIS_CAMUNDA_ZEEBE_URL" \
		-Dquarkus.oidc-client.auth-server-url="$ESTHESIS_KEYCLOAK_AUTH_SERVER_URL" \
		-Desthesis.oidc.authority="$ESTHESIS_KEYCLOAK_AUTH_SERVER_URL"
fi
