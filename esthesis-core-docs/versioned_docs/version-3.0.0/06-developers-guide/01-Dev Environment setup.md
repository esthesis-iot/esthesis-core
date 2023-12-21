# Dev environment setup

In the following sections you can see how to set up a development environment for esthesis Core. The
development environment varies drastically from the production setup, as its purpose is to allow
developers to quickly implement and test new features.

Setting up your development environment is relatively easy, as you can use the production
environment
Helm charts; with a tweak. The major difference between using Helm to deploy on production and
creating
a development environment in your machine is that all esthesis services should run locally on your
machine, instead of being deployed to the Kubernetes cluster.

## Requirements

- A dev Kubernetes cluster with at least 16GB of RAM. We have spent more hours that we would like to
	remember trying to make TCP/UDP port forwarding and ingresses in Minikube, Kind, Rancher Desktop, etc.
	work across Linux, Windows, and macOS Intel/Apple with various degrees of success. Considering firing
	up an Ubuntu server while preselecting Microk8s takes at most 15', this is our recommendation for a
	development environment using your favourite VM manager for your OS.
- [Helm](https://helm.sh)
- [Helmfile](https://github.com/helmfile/helmfile)
- Many of the build and helper scripts are written in Bash, so you need a Bash shell. If you are on
	Windows, you can use [WSL](https://docs.microsoft.com/en-us/windows/wsl/install-win10) or
	[Cygwin](https://www.cygwin.com/).

## Kubernetes initialisation

SSH to your Microk8s VM and enable the following addons:
- `microk8s enable dns`
- `microk8s enable hostpath-storage`
- `microk8s enable ingress`
- `microk8s enable registry`
- `microk8s enable metallb`: Use an IP range routable from your development machine. If you are
located in a corporate network, please check with your network administrators first.

## Installation

- Install the supporting dependencies in `esthesis-helm/esthesis-core-deps`:
	```shell
	helmfile -e dev sync
	```

	:::tip
	Once you have performed the initial installation, you can use the `--skip-deps` flag to skip the skip dependencies check, therefore speeding up your deployment.
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
- Update your `hosts` file by executing `hosts-file-update.sh`.


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

Before launching the services in your local machine, make sure that your local Kubernetes configuration
points to the development cluster. You can do this by running `microk8s config` and copying the
output to `~/.kube/config`. This is needed as some of the services need to access the Kubernetes API.

### Automation
Do yourself a favour and prepare some automation to perform all the above steps for you. Depending
on your OS, you might have different options. For macOS, we use [itomate](https://github.com/kamranahmedse/itomate),
e.g.:

![itomate](/img/docs/dev-guide/itomate.gif)

## Resources
Provided you have successfully updated your `hosts` file, you can access the following resources:

| Resource         | URL/host                                  | Credentials                       |
|------------------|-------------------------------------------|-----------------------------------|
| esthesis Core UI | http://localhost:4200                     | esthesis-admin / esthesis-admin   |
| MongoDB          | mongodb.esthesis.localdev:27017           | esthesis-system / esthesis-system |
| Kafka            | kafka.esthesis.localdev:9094              |                                   |
| Keycloak         | http://keycloak.esthesis.localdev         | esthesis-system / esthesis-system |
| APISIX Dashboard | http://apisix-dashboard.esthesis.localdev | esthesis-system / esthesis-system |
| Redis            | redis.esthesis.localdev:6379/0            | (empty) / esthesis-system         |
| MQTT             | mqtt.esthesis.localdev:1883               |                                   |
| Grafana          | http://grafana.esthesis.localdev          | esthesis-system / esthesis-system |
| InfluxDB HTTP    | http://influxdb.esthesis.localdev    	    | esthesis-system / esthesis-system |
| InfluxDB         | influxdb.esthesis.localdev:8088    	      | -                                 |
| Docker Registry  | registry.esthesis.localdev                | -                                 |

## Notes
1. Before trying to log in to the application open the Keycloak URL into your browser in order to
	 accept the self-signed certificate. Otherwise, the first redirect from the application's UI to
	 Keycloak will fail.
2. There is a convenience script `destroy.sh` in the root of each helm package. You can use it to fully
	 erase all installations performed for that particular package together with any additional Kubernetes
	 resources that do not get automatically deleted by uninstalling the Helm chart (for example, PVCs).
3. Having everything in one single node may require you to increase certain OS limits, here are the
   ones we have found to be relevant in Ubuntu server 23.04:
	```shell
	sudo sysctl fs.inotify.max_user_instances=1280
	sudo sysctl fs.inotify.max_user_watches=655360
	```

## Committing code
Please note we are using [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/).
There are plugins for all major IDEs, so you can easily follow the convention.

## Follow up
Continue with the Startup Guide to learn how to start using esthesis Core.
