# Working with a private Container Registry
When working in dev mode you run all services in your local machine, so there is no need to
push images to a container registry. However, when you want to test your changes in a production-like
Kubernetes environment you need to push your images to a container registry, so that your
production-like Kubernetes cluster can pull them from. Considering esthesis Core maintains many
services with support of multiple architectures, pushing all those images to e.g. Docker Hub can take a
long time.

To speed up the process you can use the private Container Registry provided by the dependencies Helm Chart.
The chart will deploy a private registry in your Kubernetes cluster as a NodePort Service. The service
will be assigned a random port, so you should take note of the IP of your worker node as well as the
assigned port. You can then use the `publish.sh` script to build and push your images to the private
registry.

## Building and pushing images
When using the `publish.sh` script to prepare your images, you can define the `ESTHESIS_REGISTRY_URL`
environment variable to point to a private registry. This variable should point to the IP address,
port, and username of your private registry, for example:
```shell
ESTHESIS_REGISTRY_URL=192.168.10.47:32000/esthesis ./publish.sh
```

Note that since your private registry will be insecure, you need to add it to the insecure registries
list. To do so, create a file `buildkit.toml` and place it in the same level as the `publish.sh` script.
The contents of this file should be similar to:
```toml
insecure-entitlements = [ "network.host", "security.insecure"]
[registry."192.168.10.47:32000"]
  http = true
  insecure = true
```

## Using the private registry in testing a production-like installation
When testing a production-like installation, you can configure the Helm charts to use the images from
your private registry instead of e.g. Docker Hub. To do so, you can define the `ESTHESIS_REGISTRY_URL`
environment variable to point to the private registry. This variable should point to the IP address,
port, and username of your private registry, for example:
```shell
ESTHESIS_REGISTRY_URL=192.168.10.47:32000/esthesis helmfile sync
```

:::tip
When using the Helm charts without the `-env dev` flag, the charts will automatically
use multi-node deployments. If you are testing in a single-node Kubernetes cluster, you need to also
define `ESTHESIS_SINGLE_NODE=true` environmental variable.
:::

## Deploying Dataflows
To use a private registry when deploying a Dataflow, you can use the
"Custom Docker Registry" field in the Dataflow definition screen specifying the same registry
coordinates you used when building the services, for example `192.168.10.47:32000/esthesis`. Similarly
to when building the services, you should configure your Kubernetes distribution to be able to pull
from that insecure registry. The exact way this is done depends on the Kubernetes distribution you
are using.

Depending on your Kubernetes distribution, you might be able to get away with adding the registry to
the insecure registries list by replacing the IP address of your registry with `localhost`, for
example `localhost:32000/esthesis`.
```
