# Working with a private Container Registry
When working in dev mode you run all services in your local machine, so there is no need to
push images to a container registry. However, when you want to test your changes in a production-like
Kubernetes environment you need to push your images to a container registry, so that your
production-like Kubernetes cluster can pull them from. Considering esthesis Core maintains many
services with support of multiple architectures, pushing all those images to e.g. Docker Hub can take a
long time.

To speed up the process you can use the Container Registry provided by Microk8s.

## Building and pushing images
When using the `publish.sh` script to prepare your images, you can define the `ESTHESIS_REGISTRY_URL`
environment variable to point to a private registry. This variable should point to the IP address
of your Microk8s VM using port 32000. For example:
```shell
ESTHESIS_REGISTRY_URL=192.168.10.47:32000 ./publish.sh
```

## Using the private registry in testing a production-like installation
When testing a production-like installation, you can configure the Helm charts to use the images from
your private registry instead of e.g. Docker Hub. To do so, you can define the `ESTHESIS_REGISTRY_URL`
environment variable to point to the private registry. This variable should point to the IP address
of your Microk8s VM using port 32000. For example:
```shell
ESTHESIS_REGISTRY_URL=192.168.10.47:32000 helmfile sync
```

:::tip
When using the Helm charts without the `-env dev` flag, the charts will automatically
use multi-node deployments. If you are testing in a single-node Kubernetes cluster, you need to also
define `ESTHESIS_SINGLE_NODE=true` environmental variable.
:::

## Deploying Dataflows
To use a private registry when deploying a Dataflow, you can use the
"Custom Docker Registry" field in the Dataflow definition screen specifying `localhost:32000`.
