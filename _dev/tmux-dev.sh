#!/usr/bin/env bash

# Set ESTHESIS_TMUX_PAUSE environment variable to true to pause before starting each service.

# Check if the number of arguments is not equal to 1
if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <namespace>"
    exit 1
fi

# The name of this tmux session.
SESSION=esthesis-dev

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
tmux send-keys "cd $(pwd)/..; cd esthesis-core-ui; eval $PAUSE; npm start" C-m

# Start services
tmux split-window -v
tmux select-pane -t 0.1 -T "SRV-ABOUT"
tmux pipe-pane -o -t 0.1 "sed -u 's/^/\[SRV-ABOUT   \] /' | cat >> $LOGS"
tmux send-keys "cd $(pwd)/..; cd esthesis-core-backend/services/srv-about; eval $PAUSE; ./dev-about.sh" C-m
tmux select-layout tiled

tmux split-window -v
tmux select-pane -t 0.2 -T "SRV-AGENT"
tmux pipe-pane -o -t 0.2 "sed -u 's/^/\[SRV-AGENT   \] /' | cat >> $LOGS"
tmux send-keys "cd $(pwd)/..; cd esthesis-core-backend/services/srv-agent; eval $PAUSE; ./dev-agent.sh" C-m
tmux select-layout tiled

tmux split-window -v
tmux select-pane -t 0.3 -T "SRV-APPLICATION"
tmux pipe-pane -o -t 0.3 "sed -u 's/^/\[SRV-APPLICAT\] /' | cat >> $LOGS"
tmux send-keys "cd $(pwd)/..; cd esthesis-core-backend/services/srv-application; eval $PAUSE; ./dev-application.sh" C-m
tmux select-layout tiled

tmux split-window -v
tmux select-pane -t 0.4 -T "SRV-AUDIT"
tmux pipe-pane -o -t 0.4 "sed -u 's/^/\[SRV-AUDIT   \] /' | cat >> $LOGS"
tmux send-keys "cd $(pwd)/..; cd esthesis-core-backend/services/srv-audit; eval $PAUSE; ./dev-audit.sh" C-m
tmux select-layout tiled

tmux split-window -v
tmux select-pane -t 0.5 -T "SRV-CAMPAIGN"
tmux pipe-pane -o -t 0.5 "sed -u 's/^/\[SRV-CAMPAIGN\] /' | cat >> $LOGS"
tmux send-keys "cd $(pwd)/..; cd esthesis-core-backend/services/srv-campaign; eval $PAUSE; ./dev-campaign.sh" C-m
tmux select-layout tiled

tmux split-window -v
tmux select-pane -t 0.6 -T "SRV-COMMAND"
tmux pipe-pane -o -t 0.6 "sed -u 's/^/\[SRV-COMMAND \] /' | cat >> $LOGS"
tmux send-keys "cd $(pwd)/..; cd esthesis-core-backend/services/srv-command; eval $PAUSE; ./dev-command.sh" C-m
tmux select-layout tiled

tmux split-window -v
tmux select-pane -t 0.7 -T "SRV-CRYPTO"
tmux pipe-pane -o -t 0.7 "sed -u 's/^/\[SRV-CRYPTO  \] /' | cat >> $LOGS"
tmux send-keys "cd $(pwd)/..; cd esthesis-core-backend/services/srv-crypto; eval $PAUSE; ./dev-crypto.sh" C-m
tmux select-layout tiled

tmux split-window -v
tmux select-pane -t 0.8 -T "SRV-DATAFLOW"
tmux pipe-pane -o -t 0.8 "sed -u 's/^/\[SRV-DATAFLOW\] /' | cat >> $LOGS"
tmux send-keys "cd $(pwd)/..; cd esthesis-core-backend/services/srv-dataflow; eval $PAUSE; ./dev-dataflow.sh" C-m
tmux select-layout tiled

tmux split-window -v
tmux select-pane -t 0.9 -T "SRV-DEVICE"
tmux pipe-pane -o -t 0.9 "sed -u 's/^/\[SRV-DEVICE  \] /' | cat >> $LOGS"
tmux send-keys "cd $(pwd)/..; cd esthesis-core-backend/services/srv-device; eval $PAUSE; ./dev-device.sh" C-m
tmux select-layout tiled

tmux split-window -v
tmux select-pane -t 0.10 -T "SRV-DT"
tmux pipe-pane -o -t 0.10 "sed -u 's/^/\[SRV-DT      \] /' | cat >> $LOGS"
tmux send-keys "cd $(pwd)/..; cd esthesis-core-backend/services/srv-dt; eval $PAUSE; ./dev-dt.sh" C-m
tmux select-layout tiled

tmux split-window -v
tmux select-pane -t 0.11 -T "SRV-INFRASTRUCTURE"
tmux pipe-pane -o -t 0.11 "sed -u 's/^/\[SRV-INFRASTR\] /' | cat >> $LOGS"
tmux send-keys "cd $(pwd)/..; cd esthesis-core-backend/services/srv-infrastructure; eval $PAUSE; ./dev-infrastructure.sh" C-m
tmux select-layout tiled

tmux split-window -v
tmux select-pane -t 0.12 -T "SRV-KUBERNETES"
tmux pipe-pane -o -t 0.12 "sed -u 's/^/\[SRV-KUBERNET\] /' | cat >> $LOGS"
tmux send-keys "cd $(pwd)/..; cd esthesis-core-backend/services/srv-kubernetes; eval $PAUSE; ./dev-kubernetes.sh" C-m
tmux select-layout tiled

tmux split-window -v
tmux select-pane -t 0.13 -T "SRV-PROVISIONING"
tmux pipe-pane -o -t 0.13 "sed -u 's/^/\[SRV-PROVISIO\] /' | cat >> $LOGS"
tmux send-keys "cd $(pwd)/..; cd esthesis-core-backend/services/srv-provisioning; eval $PAUSE; ./dev-provisioning.sh" C-m
tmux select-layout tiled

tmux split-window -v
tmux select-pane -t 0.14 -T "SRV-PUBLIC-ACCESS"
tmux pipe-pane -o -t 0.14 "sed -u 's/^/\[SRV-PUBLICAC\] /' | cat >> $LOGS"
tmux send-keys "cd $(pwd)/..; cd esthesis-core-backend/services/srv-public-access; eval $PAUSE; ./dev-public-access.sh" C-m
tmux select-layout tiled

tmux split-window -v
tmux select-pane -t 0.15 -T "SRV-SECURITY"
tmux pipe-pane -o -t 0.15 "sed -u 's/^/\[SRV-SECURITY\] /' | cat >> $LOGS"
tmux send-keys "cd $(pwd)/..; cd esthesis-core-backend/services/srv-security; eval $PAUSE; ./dev-security.sh" C-m
tmux select-layout tiled

tmux split-window -v
tmux select-pane -t 0.16 -T "SRV-SETTINGS"
tmux pipe-pane -o -t 0.16 "sed -u 's/^/\[SRV-SETTINGS\] /' | cat >> $LOGS"
tmux send-keys "cd $(pwd)/..; cd esthesis-core-backend/services/srv-settings; eval $PAUSE; ./dev-settings.sh" C-m
tmux select-layout tiled

tmux split-window -v
tmux select-pane -t 0.17 -T "SRV-TAG"
tmux pipe-pane -o -t 0.17 "sed -u 's/^/\[SRV-TAG     \] /' | cat >> $LOGS"
tmux send-keys "cd $(pwd)/..; cd esthesis-core-backend/services/srv-tag; eval $PAUSE; ./dev-tag.sh" C-m
tmux select-layout tiled

# Start Docusaurus
tmux split-window -v
tmux select-pane -t 0.18 -T "Docusaurus"
tmux pipe-pane -o -t 0.18 "sed -u 's/^/\[Docusaurus  \] /' | cat >> $LOGS"
tmux send-keys "cd $(pwd)/..; cd esthesis-core-docs; eval $PAUSE; npm start" C-m
tmux select-layout tiled

# Start Promtail
tmux split-window -v
tmux select-pane -t 0.19 -T "Promtail"
tmux pipe-pane -o -t 0.19 "sed -u 's/^/\[Promtail    \] /' | cat >> $LOGS"
tmux send-keys "eval $PAUSE; promtail --config.file=./promtail/config.yaml" C-m
tmux select-layout tiled

# Start kubefwd
tmux split-window -v
tmux select-pane -t 0.20 -T "Kubefwd"
tmux pipe-pane -o -t 0.20 "sed -u 's/^/\[Kubefwd     \] /' | cat >> $LOGS"
tmux send-keys "eval $PAUSE; sudo -E kubefwd svc -n $1 -d $1 -l 'app.kubernetes.io/name in (mongodb,keycloak,kafka,influxdb,redis,zeebe-gateway,grafana,mosquitto,kafka-ui,grafana-loki,grafana-tempo)'" C-m
tmux select-layout tiled

# Start log monitoring
tmux split-window -f
tmux select-pane -t 0.21 -T "Log monitoring"
tmux send-keys "tail -f $LOGS" C-m

# Attach to session
tmux a


