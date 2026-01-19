#!/usr/bin/env bash

####################################################################################################
# Generic service starter.
#
# Arguments:
#	 	LAUNCH_FOLDER		: The location of the folder to launch the service from (default: .).
#	 	WEB_PORT				: The port to run the embedded web server on (default: 0, not enabled)
#	 	DEBUG_PORT			: Debug port to run the service on (default: 0, not enabled).
#   PROFILES				: Additional comma-separated profiles to activate (e.g. 'test,prod').
#	  OIDC						: true/false indicating whether to enable OIDC configuration (default: false).
#	  OIDC_CLIENT			: true/false indicating whether to enable OIDC client configuration (default: false).
#   MONGODB					: true/false indicating whether to enable MongoDB configuration (default: false).
#   REDIS						: true/false indicating whether to enable Redis configuration (default: false).
#   KAFKA						: true/false indicating whether to enable Kafka configuration (default: false).
#   ZEEBE						: true/false indicating whether to enable Zeebe configuration (default: false).
#   GRAFANA_TEMPO		: true/false indicating whether to enable Grafana Tempo configuration (default: false).
####################################################################################################

# Check the type of dev environment used, and prepare environment-specific variables.
if [[ -z "${ESTHESIS_DEV_ENV}" ]]; then
  ESTHESIS_DEV_ENV="k8s"
else
  if [[ "${ESTHESIS_DEV_ENV}" != "docker" && "${ESTHESIS_DEV_ENV}" != "k8s" ]]; then
    echo "ESTHESIS_DEV_ENV is wrongly defined, it should be 'docker' or 'k8s. Exiting..."
    exit 1
  fi
fi
if [[ "${ESTHESIS_DEV_ENV}" == "docker" ]]; then
	ESTHESIS_OIDC_AUTHORITY="http://localhost:8080/realms/esthesis"
else
	NAMESPACE=$(kubens -c)
	if [ "$NAMESPACE" = "" ]; then
  	echo "***ERROR Could not find current namespace. Exiting."
  	exit 1
  fi
  ESTHESIS_OIDC_AUTHORITY="http://keycloak-headless.$NAMESPACE/realms/esthesis"
fi

# Init arguments.
LAUNCH_FOLDER="."
WEB_PORT=0
DEBUG_PORT=0
PROFILES="dev"
OIDC="false"
OIDC_CLIENT="false"
MONGODB="false"
REDIS="false"
KAFKA="false"
ZEEBE="false"
GRAFANA_TEMPO="false"
MVNW_DIR="."
while [[ "$#" -gt 0 ]]; do
    case $1 in
        LAUNCH_FOLDER=*) LAUNCH_FOLDER="${1#*=}"; shift ;;
        WEB_PORT=*) WEB_PORT="${1#*=}"; shift ;;
        DEBUG_PORT=*) DEBUG_PORT="${1#*=}"; shift ;;
      	PROFILES=*) PROFILES="${1#*=}"; shift ;;
      	OIDC=*) OIDC="${1#*=}"; shift ;;
      	OIDC_CLIENT=*) OIDC_CLIENT="${1#*=}"; shift ;;
      	MONGODB=*) MONGODB="${1#*=}"; shift ;;
      	REDIS=*) REDIS="${1#*=}"; shift ;;
      	KAFKA=*) KAFKA="${1#*=}"; shift ;;
      	ZEEBE=*) ZEEBE="${1#*=}"; shift ;;
      	GRAFANA_TEMPO=*) GRAFANA_TEMPO="${1#*=}"; shift ;;
				MVNW_DIR=*) MVNW_DIR="${1#*=}"; shift ;;
        *) echo "***ERROR Unknown parameter passed: $1. Exiting."; exit 1 ;;
    esac
done
LAUNCH_FOLDER=$(realpath "$LAUNCH_FOLDER")
MVNW_DIR=$(realpath "$MVNW_DIR")
if [ ! -d "$LAUNCH_FOLDER" ]; then
	echo "***ERROR Could not find launch folder: $LAUNCH_FOLDER. Exiting."
	exit 1
fi
if [ ! -d "$MVNW_DIR" ] || [ ! -f "$MVNW_DIR"/mvnw ]; then
	echo "***ERROR Could not find Maven Wrapper directory or Maven Wrapper script at $MVNW_DIR. Exiting."
	exit 1
fi

# Check if Quarkus console should be enabled.
CONSOLE=true
if [ "$TERM_PROGRAM" = tmux ]; then
  CONSOLE=false
  echo "Disabling Quarkus console in tmux."
fi

# Set environment variables.
echo "**********************************************************************************************"
echo "JDK: "$(java -version 2>&1 | head -n 1 | sed 's/^[ \t]*//;s/[ \t]*$//')
if [ "$ESTHESIS_DEV_ENV" = "k8s" ]; then
	echo "Namespace: $NAMESPACE."
else
	echo "Running in Docker environment."
fi
echo "Launching Quarkus service from: $LAUNCH_FOLDER."
echo "Using Maven Wrapper: $MVNW_DIR."
if [ -n "$WEB_PORT" ]; then
	echo "Running Quarkus web on port: $WEB_PORT."
fi
if [ -n "$DEBUG_PORT" ]; then
	echo "Debugging Quarkus on port: $DEBUG_PORT."
fi
echo "Using profiles: $PROFILES."
if [ "$OIDC" = "true" ] || [ "$OIDC_CLIENT" = "true" ]; then
	if [ "$ESTHESIS_DEV_ENV" = "k8s" ]; then
		ESTHESIS_OIDC_SERVER_URL="http://keycloak-headless.$NAMESPACE/realms/esthesis"
	else
		ESTHESIS_OIDC_SERVER_URL="http://localhost:8080/realms/esthesis"
	fi
	if [ "$OIDC" = "true" ]; then
		echo "Enabling OIDC configuration at: $ESTHESIS_OIDC_SERVER_URL."
	fi
	if [ "$OIDC_CLIENT" = "true" ]; then
		echo "Enabling OIDC client configuration at: $ESTHESIS_OIDC_SERVER_URL."
	fi
fi
if [ "$MONGODB" = "true" ]; then
	if [ "$ESTHESIS_DEV_ENV" = "docker" ]; then
		ESTHESIS_MONGODB_URL="mongodb://localhost:27017"
	else
		ESTHESIS_MONGODB_URL="mongodb://mongodb-rs0.$NAMESPACE:27017"
	fi
	echo "Enabling MongoDB configuration at: $ESTHESIS_MONGODB_URL."
fi
if [ "$REDIS" = "true" ]; then
	ESTHESIS_REDIS_URL="redis://:esthesis-system@redis.$NAMESPACE:6379/0"
	echo "Enabling Redis configuration at: $ESTHESIS_REDIS_URL."
fi
if [ "$KAFKA" = "true" ]; then
	if [ "$ESTHESIS_DEV_ENV" = "docker" ]; then
		ESTHESIS_KAFKA_URL="localhost:9092"
	else
		ESTHESIS_KAFKA_URL="kafka-kafka-bootstrap.$NAMESPACE:9092"
	fi
	echo "Enabling Kafka configuration at: $ESTHESIS_KAFKA_URL."
fi
if [ "$ZEEBE" = "true" ]; then
	ESTHESIS_CAMUNDA_ZEEBE_URL="zeebe-gateway.$NAMESPACE:26500"
	echo "Enabling Zeebe configuration at: $ESTHESIS_CAMUNDA_ZEEBE_URL."
fi
if [ "$GRAFANA_TEMPO" = "true" ]; then
	ESTHESIS_GRAFANA_TEMPO_DISTRIBUTOR_URL="http://grafana-tempo-distributor.$NAMESPACE:4317"
	echo "Enabling Grafana Tempo configuration at: $ESTHESIS_GRAFANA_TEMPO_DISTRIBUTOR_URL."
fi
echo "**********************************************************************************************"

# Run Quarkus.
cd "$LAUNCH_FOLDER" || exit
# shellcheck disable=SC2046
"$MVNW_DIR"/mvnw quarkus:dev -q \
	-Dquarkus.profile="$PROFILES" \
	-Dquarkus.console.enabled="$CONSOLE" \
	$( [ -n "$WEB_PORT" ] && echo "-Dquarkus.http.port=$WEB_PORT" ) \
	$( [ -n "$DEBUG_PORT" ] && echo "-Ddebug=$DEBUG_PORT" ) \
	$( [ -n "$ESTHESIS_MONGODB_URL" ] && echo "-Dquarkus.mongodb.connection-string=$ESTHESIS_MONGODB_URL" ) \
	$( [ -n "$ESTHESIS_GRAFANA_TEMPO_DISTRIBUTOR_URL" ] && echo "-Dquarkus.otel.exporter.otlp.traces.endpoint=$ESTHESIS_GRAFANA_TEMPO_DISTRIBUTOR_URL" ) \
	$( [ -n "$ESTHESIS_REDIS_URL" ] && echo "-Dquarkus.redis.hosts=$ESTHESIS_REDIS_URL" ) \
	$( [ -n "$ESTHESIS_KAFKA_URL" ] && echo "-Dkafka.bootstrap.servers=$ESTHESIS_KAFKA_URL" ) \
	$( [ -n "$ESTHESIS_CAMUNDA_ZEEBE_URL" ] && echo "-Dquarkus.zeebe.client.broker.gateway-address=$ESTHESIS_CAMUNDA_ZEEBE_URL" ) \
	$( [ "$OIDC" = "true" ] && echo "-Dquarkus.oidc.auth-server-url=$ESTHESIS_OIDC_SERVER_URL" ) \
	$( [ "$OIDC_CLIENT" = "true" ] && echo "-Dquarkus.oidc-client.auth-server-url=$ESTHESIS_OIDC_SERVER_URL" ) \
	$( [ "$OIDC_CLIENT" = "true" ] && echo "-Dquarkus.oidc-client.grant-options.password.username=esthesis-system" ) \
	$( [ "$OIDC_CLIENT" = "true" ] && echo "-Dquarkus.oidc-client.grant-options.password.password=esthesis-system" ) \
	$( [ "$OIDC_CLIENT" = "true" ] && echo "-Dquarkus.oidc-client.client-id=esthesis" ) \
	$( [ "$OIDC_CLIENT" = "true" ] && echo "-Dquarkus.oidc-client.grant.type=password" ) \
	-Desthesis.oidc.redirect-url="http://localhost:4200/callback" \
	-Desthesis.oidc.post-logout-redirect-uri="http://localhost:4200/logged-out" \
	-Desthesis.oidc.authority="$ESTHESIS_OIDC_AUTHORITY"
