# Working with a private Container Registry
When working in dev mode you run all services in your local machine, so there is no need to
push images to a container registry. However, when you want to test your changes in a production-like
Kubernetes environment you need to push your images to a container registry, so that your
production-like Kubernetes cluster can pull them from. Considering esthesis CORE maintains many
services with support of multiple architectures, pushing all those images to e.g. Docker Hub can take a
long time.

To speed up the process you can use the private Container Registry provided by esthesis dependencies
Helm Chart.
The chart will deploy a private registry in your Kubernetes cluster as a NodePort Service. The service
will be assigned a random port, so you should take note of the IP of your worker node as well as the
assigned port. You can then use the `publish.sh` script to build and push your images to the private
registry.

## Building and pushing images
When using the `publish.sh` script to prepare your images, you can define the `ESTHESIS_REGISTRY_URL`
environment variable to point to a private registry. This variable should point to the IP address,
port, and user/project name of your private registry, for example:
```shell
ESTHESIS_REGISTRY_TYPE=open ESTHESIS_REGISTRY_URL=192.168.50.211/esthesis ./publish.sh
```

<tip>
192.168.50.211:5000 is used as an example in all following configurations too, change it according
to your own setup.
</tip>

Note that since your private registry will be insecure, you need to add it to the insecure registries
list. To do so, create a file `buildkit.toml` and place it in the same level as the `publish.sh` script.
The contents of this file should be similar to:
```
insecure-entitlements = [ "network.host", "security.insecure"]
[registry."192.168.50.211:5000"]
  http = true
  insecure = true
```

In case you are pushing to a private registry requiring authentication, you can define the following
environment variables:
```shell
ESTHESIS_REGISTRY_TYPE=auth
ESTHESIS_REGISTRY_USERNAME=<username>
ESTHESIS_REGISTRY_PASSWORD=<password>
````

## Using the private registry in testing a production-like installation
When testing a production-like installation, you can configure the Helm charts to use the images from
your private registry instead of e.g. Docker Hub. To do so, you can define the `ESTHESIS_REGISTRY_URL`
environment variable to point to the private registry. This variable should point to the IP address,
port, and user/project name of your private registry, for example:
```shell
ESTHESIS_REGISTRY_URL=192.168.50.211:5000/esthesis helmfile sync -n esthesis
```

<tip>

1. If while working with a private registry you are also deploying on a Kubernetes cluster that does
not have access to proper certificates, you can also set `QUARKUS_OIDC_TLS_VERIFICATION=none` while
deploying the Helm charts.
</tip>

## Deploying Dataflows
To use a private registry when deploying a Dataflow, you can use the "Custom Docker Registry" field
in the Dataflow definition screen specifying the same registry coordinates you used when building
the services, for example `192.168.50.211:5000/esthesis`. Similarly to when building the services,
you should configure your Kubernetes distribution to be able to pull from that insecure registry.
The exact way this is done depends on the Kubernetes distribution you are using.

- K3S:\
	Edit or create `/etc/rancher/k3s/registries.yaml` and add the following lines:
	```yaml
	mirrors:
  "192.168.50.211:5000":
    endpoint:
      - "http://192.168.50.211:5000"
	```
 	Restart the K3S service with `sudo systemctl restart k3s.service`.

Depending on your Kubernetes distribution, you might be able to get away with adding the registry to
the insecure registries list by replacing the IP address of your registry with `localhost`, for
example `localhost:32000/esthesis`.
