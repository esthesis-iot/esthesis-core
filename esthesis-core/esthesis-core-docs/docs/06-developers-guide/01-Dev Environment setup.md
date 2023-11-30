# Dev environment setup

In the following sections you can see how to set up a development environment for esthesis Core. The
development environment varies drastically from the production setup, as its purpose is to allow
developers to quickly implement and test new features.

Setting up your development environment is relatively easy, as you can use the production
environment Helm charts; with a tweak. The major difference between using Helm to deploy on production and
creating a development environment in your machine is that all esthesis services should run locally on your
machine, instead of being deployed to the Kubernetes cluster.

## Requirements

- A dev Kubernetes cluster with at least 16GB of RAM. We suggest firing up an [Ubuntu server](https://ubuntu.com/download/server)
	in the virtualisation platform of your choice, while preselecting Microk8s in the installed packages
	screen. This way you can have a "real" Kubernetes, single-node cluster, using the same
	Kubernetes distribution the rest of us are also using, so we can provide appropriate guidance and
	setup instructions that work across the board.
- [Helm](https://helm.sh)
- [Helmfile](https://github.com/helmfile/helmfile)
- Many of the build and helper scripts are written for a Unix shell. If you are on
	Windows, you can use [WSL](https://docs.microsoft.com/en-us/windows/wsl/install-win10) or [Cygwin](https://www.cygwin.com/).

## Kubernetes initialisation

SSH to your Microk8s VM and enable the following addons:
- `microk8s enable dns`
- `microk8s enable hostpath-storage`
- `microk8s enable registry`

## Installation

- Install the supporting dependencies in `esthesis-helm/esthesis-core-deps`:
	```shell
	helmfile -e dev sync
	```

	:::tip
	Once you have performed the initial installation, you can use the `--skip-deps` flag to skip the
  dependencies check, therefore speeding up your deployment.
	:::

- Install the application components in `esthesis-helm/esthesis-core`:
	```shell
	DEV_HOST=192.168.100.102 helmfile -e dev sync
	```

	:::caution
	You need to specify the IP address of your development machine in the `DEV_HOST` environment
	variable. This is needed so that the API gateway (APISIX) knows where to forward the requests to
	(since in `dev` setup the services run on your own machine, not in Kubernetes).
	:::

## Access to resources
To proxy the Kubernetes services of the project to your local machine for development purposes, you
can use [kubefwd](https://kubefwd.com) and execute:
```shell
sudo -E kubefwd svc -d esthesis
```
kubefwd will proxy all services to your localhost and create local DNS entries for them. The table
below summarises the resources you can access after running the above command. Note that if you have
deployed the services in a different namespace than `esthesis`, you need to adjust the namespace
element in the table below:

| Resource          | URL/host                         | Credentials                       |
|-------------------|----------------------------------|-----------------------------------|
| APISIX Dashboard  | http://apisix-dashboard.esthesis | esthesis-system / esthesis-system |
| Redis             | redis-master.esthesis:6379/0     | (empty) / esthesis-system         |
| Mosquitto         | mosquitto.esthesis:1883          |                                   |
| Grafana           | http://grafana.esthesis:3000     | esthesis-system / esthesis-system |
| InfluxDB Admin UI | http://influxdb.esthesis:8086    | esthesis-system / esthesis-system |
| InfluxDB          | influxdb.esthesis:8088           | -                                 |
| MongoDB           | mongodb.esthesis:27017           | esthesis-system / esthesis-system |
| esthesis Core UI  | http://localhost:4200            | esthesis-admin / esthesis-admin   |
| Keycloak          | http://keycloak.esthesis         | esthesis-system / esthesis-system |
| Kafka             | kafka.esthesis:9095              |                                   |

## Running the services
The above installation will prepare all the necessary components to support esthesis Core. The actual
backend services as well as the UI, however, will not be installed in Kubernetes. You need to run
these in your local machine. Note that we did try Quarkus' remote development mode, but it was a bit
finicky, and we ended up losing time when it was not working or not picking up changes correctly.

### Frontend
You can start the Angular frontend by running `npm start` in `esthesis-core-ui` directory.

### Backend
Each backend service comes with its own `dev-{service-name}.sh` script that you can use to start the
service in development mode. You need to run each of the services in a separate terminal window.

### Automation
Starting up (and restarting) all those services manually can be a tedious task. We have prepared a
tmux script that you can use to start all services in a single terminal window in multiple panes,
while merging all log output into another pane. You can find the script in `_dev/tmux-dev.sh`. We
also provide a `.tmux.conf` file, in case you want to replicate our own tmux look and feel.

![tmux](/img/docs/dev-guide/tmux.gif)

:::tip
You can quickly terminate all services by issuing `tmux kill-session -t esthesis-dev`.
:::

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

## Committing code
We are using [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/).
There are plugins for all major IDEs, so you can easily follow the convention.
