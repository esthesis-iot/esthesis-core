# Working with a private Container Registry
When working in dev mode you run all services in your local machine, so there is no need to
push images to a registry. However, when you want to test your changes in a production-like
Kubernetes environment you need to push your images to a registry, so that your
production-like Kubernetes cluster can pull them from. Considering esthesis Core maintains many
services with support of multiple architectures, pushing all those images to e.g. Docker Hub can take a
long time.

To speed up the process you can use a Harbor as a local registry.

## Building and pushing images
When using the `publish.sh` scripts to prepare your images, you can define the `ESTHESIS_REGISTRY_URL`
environment variable to point to a private registry. This variable should point to the IP address
of your local Harbor instance. As Harbor requires authentication to be able to push images, you should
also define the `ESTHESIS_REGISTRY_USER` and `ESTHESIS_REGISTRY_PASSWORD` environment variables,
for example:
```shell
ESTHESIS_REGISTRY_URL=registry.esthesis.localdev \
ESTHESIS_REGISTRY_USER=admin \
ESTHESIS_REGISTRY_PASSWORD=esthesis-system \
./publish.sh
```

## Using the private registry in testing a production-like installation
When testing a production-like installation, you can configure the Helm charts to use the images from
your private registry instead of e.g. Docker Hub. To do so, you can define the `ESTHESIS_REGISTRY_URL`
environment variable to point to the private registry. This variable should point to the IP address
of your local registry, for example:
```shell
TODO
```

:::tip
When using the Helm charts without the `-env dev` flag, the charts will automatically
use multi-node deployments. If you are testing in a single-node Kubernetes cluster, you need to also
define `ESTHESIS_SINGLE_NODE=true` environmental variable.
:::

## Deploying Dataflows
To use a private registry when deploying a Dataflow, you can use the
"Custom Docker Registry" field in the Dataflow definition screen and specify `localhost:32000`.
