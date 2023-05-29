# Working with a private Docker Registry

When working in full dev mode you run all services in your local machine, so there is no need to
push images to a Docker registry. However, when you want to test your changes in a production-like
Kubernetes environment you need to push your images to a Docker registry, so that your production-like
Kubernetes cluster can pull them from. Considering esthesis Core maintains many services with support
of multiple architectures, pushing all those images to Docker Hub can take a long time.

To speed up the process you can use the Docker Registry provided by Microk8s.

## Building and pushing images
When using the `publish.sh` script to prepare your images, you can define the `ESTHESIS_REGISTRY`
environment variable to point to the private registry. This variable should point to the IP address
of your Microk8s VM using port 32000.
For example:
```shell
ESTHESIS_REGISTRY=192.168.20.23:32000 publish.sh
```

## Using the private registry in testing a production installation
When testing a production installation, you can configure the Helm charts to use the images from
your private registry instead of Docker Hub. To do so, you can define the `ESTHESIS_REGISTRY`
environment variable to point to the private registry. This variable should point to the IP address
of your Microk8s VM using port 32000.
For example:
```shell
ESTHESIS_REGISTRY=localhost:32000 helmfile sync
```

Note: When using the Helm charts without the `-env dev` flag, the charts will automatically
use multi-node deployments. If you are testing in a single-node Kubernetes cluster, you need to also
define `ESTHESIS_SINGLE_NODE=true` environmental variable.

## Deploying Dataflows
To use your private Docker Registry when deploying a Dataflow, you can use the
"Custom Docker Registry" field in the Dataflow definition screen and specify `localhost:32000`.
