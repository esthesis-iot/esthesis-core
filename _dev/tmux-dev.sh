#!/usr/bin/env bash

# Set ESTHESIS_TMUX_PAUSE environment variable to 'true' to pause before starting each service.

# If $ESTHESIS_DEV_ENV is k8s, the namespace needs to be set.
if [[ "${ESTHESIS_DEV_ENV}" == "k8s" || "${ESTHESIS_DEV_ENV}" == "" ]]; then
	if [ "$#" -ne 1 ]; then
      echo "Usage: $0 <namespace>"
      exit 1
  fi
fi

# The name of this tmux session.
SESSION=esthesis-dev

# Services startup delay (in seconds). This allows kubefwd to start before services.
SVC_STARTUP_DELAY=10

# Check if environment variable ESTHESIS_TMUX_PAUSE is set to true.
if [ "$ESTHESIS_TMUX_PAUSE" = true ]; then
	PAUSE="echo 'Press ENTER key to start...'; head -n 1 >/dev/null"
fi

# The path to the aggregated logs file.
LOGS=/tmp/esthesis-dev.log

# Delete previous logs file, if it exists.
if [ -f $LOGS ]; then
		rm $LOGS
fi

# Create Apps window.
tmux new-session -d -s $SESSION
tmux rename-window "Apps"

# Start UI
tmux select-pane -t 0.0 -T "UI"
tmux pipe-pane -o -t 0.0 "sed -u 's/^/\[UI          \] /' | cat >> $LOGS"
tmux send-keys "sleep $((SVC_STARTUP_DELAY + RANDOM % 6)); cd $(pwd)/..; cd esthesis-core-ui; eval $PAUSE; [ -n "$(command -v nvm)" ] && nvm use; npm start" C-m

## Start services
services=(
    "SRV-ABOUT        		srv-about        			dev-about.sh"
    "SRV-AGENT        		srv-agent        			dev-agent.sh"
    "SRV-APPLICATION  		srv-application  			dev-application.sh"
    "SRV-AUDIT        		srv-audit        			dev-audit.sh"
    "SRV-CAMPAIGN     		srv-campaign     			dev-campaign.sh"
    "SRV-COMMAND      		srv-command      			dev-command.sh"
    "SRV-CRYPTO       		srv-crypto       			dev-crypto.sh"
    "SRV-DATAFLOW     		srv-dataflow     			dev-dataflow.sh"
    "SRV-DEVICE       		srv-device       			dev-device.sh"
    "SRV-DT           		srv-dt           			dev-dt.sh"
    "SRV-INFRASTRUCTURE 	srv-infrastructure 		dev-infrastructure.sh"
    "SRV-KUBERNETES   		srv-kubernetes   			dev-kubernetes.sh"
    "SRV-PROVISIONING 		srv-provisioning 			dev-provisioning.sh"
    "SRV-PUBLIC-ACCESS 		srv-public-access 		dev-public-access.sh"
    "SRV-SECURITY     		srv-security     			dev-security.sh"
    "SRV-SETTINGS     		srv-settings     			dev-settings.sh"
    "SRV-TAG          		srv-tag          			dev-tag.sh"
)
pane_index=1
for service in "${services[@]}"; do
		read -r title dir script <<< "$service"
		tmux split-window -v
		tmux select-pane -t 0.$pane_index -T "$title"
		tmux pipe-pane -o -t 0.$pane_index "sed -u 's/^/[$title] /' | cat >> $LOGS"
		tmux send-keys "sleep $((SVC_STARTUP_DELAY + RANDOM % 6)); cd $(pwd)/..; cd esthesis-core-backend/services/$dir; eval $PAUSE; ESTHESIS_DEV_ENV=$ESTHESIS_DEV_ENV ./$script" C-m
		tmux select-layout tiled
		((pane_index++))
done

# Start Promtail
tmux split-window -v
tmux select-pane -t 0.18 -T "Promtail"
tmux pipe-pane -o -t 0.18 "sed -u 's/^/\[Promtail    \] /' | cat >> $LOGS"
tmux send-keys "eval $PAUSE; promtail --config.file=./promtail/config.yaml" C-m
tmux select-layout tiled

# Start kubefwd
tmux split-window -v
tmux select-pane -t 0.19 -T "Kubefwd"
tmux pipe-pane -o -t 0.19 "sed -u 's/^/\[Kubefwd     \] /' | cat >> $LOGS"
tmux send-keys "eval $PAUSE; sudo -E kubefwd svc -n $1 -d $1 -l 'app.kubernetes.io/name in (mongodb,keycloak,kafka,influxdb,redis,zeebe-gateway,grafana,mosquitto,kafka-ui,grafana-loki,grafana-tempo,docker-registry-ui,orion)'" C-m
tmux select-layout tiled

# Start log monitoring
tmux split-window -f
tmux select-pane -t 0.20 -T "Log monitoring"
tmux send-keys "tail -f $LOGS" C-m

# Attach to session
tmux a
