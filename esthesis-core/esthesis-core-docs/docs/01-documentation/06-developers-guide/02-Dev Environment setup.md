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
	remember trying to make TCP/UDP port forwarding and ingresses work in Minikube, Kind, Rancher Desktop, etc.
	work across Linux, Windows, and macOS Intel/Apple with various degrees of success. Considering firing
	up an Ubuntu server while preselecting microk8s takes at most 15', this is our recommendation for a
	development environment using your favourite VM manager for your OS.
- [Helm](https://helm.sh)
- [Helmfile](https://github.com/helmfile/helmfile)

## Kubernetes initialisation

SSH to your Kubernetes VM and initialise the following components:
- `microk8s enable dns`
- `microk8s enable hostpath-storage`
- `microk8s enable ingress`
- `microk8s enable metallb`: Use an IP range routable from your development machine. If you are
located in a corporate network, please check with your network administrators first.

## Installation - Supporting infrastructure

- Install the dependencies first in `esthesis-helm/esthesis-core-deps`:
	```shell
	helmfile -e dev sync --skip-deps
	```
- Update your `hosts` file with the

## Installation - Main
Install the application stack:

```shell
DEV_HOST=192.168.100.102 helmfile -e dev sync --skip-deps
```

### How to access the services


Take the output of the above script and append it to your `hosts` file (removing any similar entries
you might have added before).


## Notes

1. There is a convenience script `destroy` in the root of each helm package. You can use it to fully
erase all installations performed for that particular package together with any additional Kubernetes
resources that do not get automatically deleted by uninstalling the Helm chart (for example, PVCs).
