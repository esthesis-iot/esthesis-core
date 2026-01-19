# Dev environment setup

In the following sections you can see how to set up a development environment for esthesis CORE. The
development environment varies drastically from the production setup, as its purpose is to allow
developers to quickly implement and test new features.

Setting up your development environment is relatively easy, as you can use the production
environment Helm charts; with a tweak. The major difference between using Helm to deploy on production and
creating a development environment in your machine is that all esthesis services should run locally on your
machine, instead of being deployed to the Kubernetes cluster.

## Requirements

- A dev Kubernetes cluster with at least 16GB of RAM.
- [Helm](https://helm.sh)
- Many of the build and helper scripts are written for a Unix/Linux shell. If you are on
	Windows, you can use [WSL](https://docs.microsoft.com/en-us/windows/wsl/install-win10) or [Cygwin](https://www.cygwin.com/).
- [Promtail](https://github.com/grafana/loki/releases). Promtail is used to provide a local UDP GELF
	endpoint to collect logs and forward them to Loki. If you want to develop and test Loki functionality
	you need to be running Promtail with the configuration file in `_dev/promtail/config.yaml`, e.g.
	`promtail --config.file=config.yaml`.
- [Kubens + Kubectx](https://github.com/ahmetb/kubectx) for easy context and namespace switching.

## Kubernetes initialisation

## Installation
Unless you are on a cluster shared with others, create and use a namespace named "esthesis", so that
you can follow the instructions below without having to change parameters. If you are using a
different namespace, replace it accordingly.

- Install the supporting dependencies in `esthesis-helm/esthesis-core-deps`:
	 ```shell
  helm install esthesis-core-deps . \
    --namespace esthesis \
    --create-namespace
  ```

	<tip>
	Once you have performed the initial installation, you can use the `--skip-deps` flag to skip the
	dependencies check, therefore speeding up your re-deployment.
	</tip>

	The following services are optional and are disabled by default in the esthesis-core-deps Helm chart:
	- kafka-ui
	- orion
	- docker-registry-ui
	- grafana-tempo
	- grafana-loki
	- grafana
  - ollama

  If you wish to enable any of these services, you can do so by setting the corresponding flag to `true` using the `--set` parameter. For example, to enable the `kafka-ui` service, you can run:
	```shell
	helm install esthesis-core-deps . \
		--namespace esthesis \
		--set charts_enabled.kafka-ui=true
	```

  If you want to enable all optional services, you can use the `--set` parameter with a comma-separated list of flags:
  ```shell
	helm install esthesis-core-deps . \
		--namespace esthesis \
		--set charts_enabled.kafka-ui=true,charts_enabled.orion=true,...
	```

- You can find the full list of available flags in the `values.yaml` file of the `esthesis-core-deps` Helm chart.

- Install the application components in `esthesis-helm/esthesis-core`:
	```shell
  helm install esthesis-core . \
    --namespace esthesis
  ```

## Access to resources
To proxy the Kubernetes services of the project to your local machine for development purposes, you
can use [kubefwd](https://kubefwd.com) and execute:
```shell
sudo -E kubefwd svc -d esthesis
```
1. kubefwd will proxy all services to your localhost and create local DNS entries for them. The table
below summarises the resources you can access after running the above command. Replace `<namespace>`
with the namespace you have deployed the services to. Note that if you have
deployed the services in a different namespace than `esthesis`, you need to adjust the namespace
element in the domains in the table below:
2. You may need to, occasionally, restart kubefwd, especially if pods get recreated often or if your
development machine went through sleep mode.
3. The Container Registry is deployed as a NodePort service with a randomly assigned port. If you
need to specify your own port, you can use the `REGISTRY_NODE_PORT` environment variable when
deploying the Helm Chart.

| **Resource**       | **URL/host**                                       | **Credentials**                   |
|--------------------|----------------------------------------------------|-----------------------------------|
| Redis              | redis.esthesis:6379/0                              | (empty) / esthesis-system         |
| Mosquitto          | mosquitto.esthesis:1883                            |                                   |
| Grafana            | http://grafana.esthesis:3000                       | esthesis-system / esthesis-system |
| InfluxDB Admin UI  | http://influxdb.esthesis:80                        | esthesis-system / esthesis-system |
| InfluxDB           | http://influxdb.esthesis:8088                      |                                   |
| MongoDB            | mongodb-rs0.esthesis:27017                         | esthesis-system / esthesis-system |
| esthesis CORE UI   | http://localhost:4200                              | esthesis-admin / esthesis-admin   |
| Keycloak           | http://keycloak-headless.esthesis                  | esthesis-system / esthesis-system |
| Kafka              | kafka-kafka-bootstrap.esthesis:9095                |                                   |
| Kafka UI           | http://kafka-ui.esthesis                           |                                   |
| Docker Registry UI | http://docker-registry-ui-user-interface.esthesis/ |                                   |
| Orion LD           | http://orionld.esthesis:1026                       |                                   |
| Ollama             | http://ollama.esthesis:11434                       |                                   |

## Running the services
The above installation will prepare all the necessary components to support esthesis CORE. The actual
backend services as well as the UI, however, will not be installed in Kubernetes. You need to run
these in your local machine. Note that we did try Quarkus' remote development mode, but it was a bit
finicky, and we ended up losing time when it was not working or not picking up changes correctly.

### Frontend
You can start the Angular frontend by running `npm start` in `esthesis-core-ui` directory.

### Backend
Each backend service comes with its own `dev-{service-name}.sh` script that you can use to start the
service in development mode. You need to run each of the services in a separate terminal window.

Many of the backend services need to access the dependency services (e.g. Keycloak, Kafka, MongoDB,
etc.) you have deployed above. To be able to figure out the domain name under which these services are
accessible, we are using internally `kubens -c`, so make sure before you start up a service you
have switched to the Kubernetes context and namespace where the services are deployed.

### Automation
Starting up (and restarting) all those services manually can be a tedious task. We have prepared a
[tmux](https://github.com/tmux/tmux/wiki) script that you can use to start all services in a single
terminal window in multiple panes, while merging all log output into another pane. You can find the
script in `_dev/tmux-dev.sh`. We also provide a `.tmux.conf` file, in case you want to replicate our
own tmux look and feel.

![tmux.gif](tmux.gif)

<tip>
You can quickly terminate all services by issuing `tmux kill-session -t esthesis-dev`.
</tip>

## Committing code
We are using [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/).
There are plugins for all major IDEs, so you can easily follow the convention.

## Notes
1. Before trying to log in to the application open the (https) Keycloak URL into your browser in order to
	 accept the self-signed certificate. Otherwise, the first redirect from the application's UI to
	 Keycloak will fail.
2. There is a convenience script `destroy.sh` in the root of each helm package. You can use it to fully
	 erase all installations performed for that particular package together with any additional Kubernetes
	 resources that do not get automatically deleted by uninstalling the Helm chart (for example, PVCs).
3. Installing all components for development under a single Kubernetes node may require you to
	 increase certain OS limits, here are the ones we have found to be relevant in Ubuntu server 23.04:
	 ```shell
	 sudo sysctl fs.inotify.max_user_instances=1280
	 sudo sysctl fs.inotify.max_user_watches=655360
	 ```
   If you face any issues building the project via Maven, you may also need to increase the number of
	 open files:
	 ```
 	 ulimit -S -n 65536
 	 ```
4. You can create a `local-env.sh` script alongside the `dev-{service}.sh` scripts to customise your
	 local development environment. If such a file exist, it will be sourced by the `dev-{service}.sh`.
5. `srv-kubernetes` service needs to be able to create new pods in your Kubernetes environment. This
	 requires a valid `~/.kube/config` file in your home directory. In case you are splitting your
	 Kubernetes config into multiple files, you can use the `KUBECONFIG` environment variable to point
	 to the config file you want to use. You can specify this variable in your `local-env.sh` file as
	 explained above.
