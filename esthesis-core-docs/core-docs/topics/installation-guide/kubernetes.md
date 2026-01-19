# Kubernetes

Esthesis CORE can be deployed on Kubernetes using the publicly available Helm charts. The Helm
charts are available on [the Helm Chart Repo](https://esthes.is/helm).

Esthesis CORE comes with a variety of different Helm charts. Some of the provided Helm charts
pertain to mandatory components, while others are optional. During the installation you can choose
which
components you want to install by enabling the relevant configuration options. You can also choose
to use already existing resources, such as a database or a message broker, instead of the
ones provided in the Helm charts.

Please note that Helm charts come with reasonable defaults; we strongly advise to
review them, so you can customize them to your needs.

## Requirements

- A Kubernetes cluster with a minimum of 3 nodes and support for Load Balancer service types as well
	as Ingress support (you can, optionally, install an nginx ingress controller using this chart).
- [Helm](https://helm.sh)

<tip>
If you are deploying esthesis CORE in a public cloud, make sue you also take into account any
specific instructions for the cloud provider you are using.
</tip>

## Configuration parameters

The following parameters can be defined from variables contained on values.yaml during installation:

<chapter title="General" id="general" collapsible="true">

`timezone`
: The container's timezone to set (note, some containers do not respect this setting).
Default: `Europe/Athens`

`esthesisLogLevel`
: The log level to be used for the esthesis components (i.e. does not affect third-party components
installed by the Helm chart).
Default: `WARN`

`imagePullSecret`
: The name of the Kubernetes Secret to use when pulling container images.

`ingressClassName`
: The name of the ingress class to use for ingress rules.

`ingressTlsSecret`
: The name of the Kubernetes TLS secret that contains the TLS certificate and private key for
securing the Ingress with HTTPS.

</chapter>

<chapter title="Accounts" id="accounts" collapsible="true">

`esthesisAdminUsername`
: The username of the Esthesis administrator.
Default: `esthesis-admin`

`esthesisAdminPassword`
: The password for the Esthesis administrator.
Default: `esthesis-admin`

`esthesisSystemUsername`
: The system-level user for Esthesis services and third-party components.
Default: `esthesis-system`

`esthesisSystemPassword`
: The password for the system-level user.
Default: `esthesis-system`

`esthesisKubernetesServiceCreateRBAC`
: Whether to create Kubernetes RBAC resources automatically.
Default: `true`

</chapter>

<chapter title="Keycloak" id="keycloak" collapsible="true">

`keycloak.enabled`
: Whether to deploy Keycloak.
Default: `true`

`keycloak.ingress.hostname`
: The external hostname for Keycloak.

`keycloak.certManager.clusterIssuer`
: Cluster-wide Cert Manager issuer name. Mutually exclusive with `keycloak.certManager.issuer`.

`keycloak.certManager.issuer`
: Namespace-scoped Cert Manager issuer name. Mutually exclusive with
`keycloak.certManager.clusterIssuer`.

</chapter>

<chapter title="MongoDB" id="mongodb" collapsible="true">

`mongodb.enabled`
: Whether to deploy MongoDB.
Default: `true`

`mongodb.urlCluster`
: Internal MongoDB connection URL for Esthesis components.
Default: `mongodb://mongodb:27017`

`mongodb.database`
: Name of the MongoDB database.
Default: `esthesiscore`

`mongodb.username`
: MongoDB user (usually inherits from `esthesisSystemUsername`).

`mongodb.password`
: MongoDB password (usually inherits from `esthesisSystemPassword`).

</chapter>

<chapter title="NGINX Ingress" id="ingnginx" collapsible="true">

`ingressNginx.enabled`
: Whether to install the nginx ingress controller.
Default: `false`

`ingressNginx.sslCertArn`
: ARN of the wildcard certificate to use.

</chapter>

<chapter title="User Interface" id="ui" collapsible="true">

`esthesisHostname`
: External hostname for Esthesis UI.

`esthesisUi.logoutUrl`
: Path to redirect users to after logout.
Default: `/logout`

`esthesisUi.certManager.clusterIssuer`
: Cert Manager cluster-wide issuer for UI TLS.

`esthesisUi.certManager.issuer`
: Cert Manager namespace-scoped issuer for UI TLS.

</chapter>

<chapter title="Redis" id="redis" collapsible="true">

`redis.enabled`
: Whether to deploy Redis.
Default: `true`

`redis.hosts`
: List of Redis endpoints.
Default: `redis:6379/0`

</chapter>

<chapter title="Mosquitto" id="mosquitto" collapsible="true">

`mosquitto.enabled`
: Whether to deploy Mosquitto.
Default: `true`

`mosquitto.mutualTls`
: Enable mutual TLS for Mosquitto.
Default: `false`

`mosquitto.superuser`
: Super-user account (should match certificate CN when TLS is enabled).
Default: `esthesis`

`mosquitto.caCert`
: Base64-encoded CA certificate.

`mosquitto.serverCert`
: Base64-encoded Mosquitto server certificate.

`mosquitto.serverKey`
: Base64-encoded Mosquitto private key.

`mosquitto.serviceType`
: Kubernetes service type to expose Mosquitto.
Default: `ClusterIP`

</chapter>

<chapter title="InfluxDB" id="influxdb" collapsible="true">

`influxdb.enabled`
: Whether to deploy InfluxDB.
Default: `true`

`influxdb.size`
: Persistent volume size for InfluxDB.
Default: `32Gi`

</chapter>

<chapter title="Kafka" id="kafka" collapsible="true">

`kafka.enabled`
: Whether to deploy Kafka.
Default: `true`

`kafka.bootstrapServers`
: List of Kafka bootstrap servers.
Default: `kafka:9092`

</chapter>

<chapter title="Camunda" id="camunda" collapsible="true">

`camunda.enabled`
: Whether to deploy Camunda.
Default: `true`

`camunda.gatewayUrlCluster`
: Internal gateway URL for Camunda Zeebe.
Default: `camunda-zeebe-gateway:26500`

</chapter>

<chapter title="Chatbot" id="chatbot" collapsible="true">
Please note that the Chatbot functionality is currently provided as a Technology Preview Feature.

`chatbot.service.enabled`
: Whether to deploy the Chatbot service.
Default: `false`

`chatbot.chatModel.provider`
: The chat model provider, `ollama` or `openai`.
Default: `ollama`

`chatbot.embeddingModel.provider`
: The embedding model provider, `ollama` or `openai`.
Default: `ollama`

`chatbot.openai.apiKey`
: OpenAI API key for the Chatbot service.
Default: ``

`chatbot.openai.chatMOdel.modelName`
: OpenAI chat model name.
Default: `gpt-3.5-turbo`

`chatbot.openai.chatModel.temperature`
: OpenAI chat model temperature.
Default: `0.1`

`chatbot.ollama.baseUrl`
: Base URL for the Ollama service.
Default: `http://ollama:11434`

`chatbot.ollama.chatModel.modelId`
: Ollama chat model ID. Use a model that supports chat and tools functionality.
Default: `qwen3:0.6b`

`chatbot.ollama.chatModel.temperature`
: Ollama chat model temperature.
Default: `0.1`

`chatbot.ollama.embeddingModel.modelId`
: Ollama embedding model ID.
Default: `nomic-embed-text`

`chatbot.ollama.embeddingModel.temperature`
: Ollama embedding model temperature.
Default: `0.1`

</chapter>

## Installation

esthesis CORE comes in two Helm charts, one installing all the required dependencies and another one
installing the application components. You can enable/disable which specific dependencies you want
to install by setting the corresponding `charts_enabled.<service>` parameter to `true` or `false`.
Do note that
although the provided dependencies are adequate to have esthesis CORE up and running, you might want
to
tune their properties or replace them altogether with your own resources to support your specific
production use case.

### Environment variables

The following list is a starting point of variables on values.yaml to set before you proceed on both
helm charts with the installation, you need to amend them to match your own environment:

#### Esthesis-core values.yaml:

```
esthesisHostname: esthesis.domain.com
timezone: "Europe/Athens"
esthesisAdminUsername: "esthesis-admin"
esthesisAdminPassword: "esthesis-admin"
esthesisSystemUsername: "esthesis-system"
esthesisSystemPassword: "esthesis-system"
esthesisReportedOidcAuthority: "http://keycloak/realms/esthesis"
esthesisReportedOidcPostLogoutUrl: "http://esthesis-core/logout"
oidcAuthServerUrl: "http://keycloak/realms/esthesis"
oidcClientAuthServerUrl: "http://keycloak/realms/esthesis"
redisHosts: "redis://redis-headless:6379"
camundaGatewayUrlCluster: "zeebe-gateway:26500"
kafkaBootstrapServers: "kafka:9092"
mongoDbUrlCluster: "mongodb://mongodb-0.mongodb-headless:27017,mongodb-1.mongodb-headless:27017"
```

#### Esthesis-core-deps values-deps.yaml:

```
timezone: "Europe/Athens"
esthesisAdminUsername: "esthesis-admin"
esthesisAdminPassword: "esthesis-admin"
esthesisSystemUsername: "esthesis-system"
esthesisSystemPassword: "esthesis-system"
mosquittoServiceType: "LoadBalancer"
```

### Supporting infrastructure

- Add the Esthesis Helm repository:
	```shell
	helm repo add esthesis https://esthes.is/helm
	helm repo update
	```

- Install the supporting dependencies (e.g. Keycloak, MongoDB, Redis, etc.):
	```shell
	helm install esthesis-core-deps esthesis/esthesis-core-deps \
		--namespace esthesis \
		--create-namespace \
		-f values-deps.yaml
	```

	Replace `values-deps.yaml` with your customized values file if needed.

### Application

- Install the main Esthesis CORE application components:
	```shell
	helm install esthesis-core esthesis/esthesis-core \
		--namespace esthesis \
		-f values.yaml
	```

	Again, replace `values.yaml` with your specific Helm values file.

## Notes

1. You need to access the UI via HTTPS, accessing it via HTTP will not work.
2. The UI is exposed under the domain you specified in the environmental variable
	 `esthesisHostname`.
3. If you are using a self-signed certificate which is not imported into your local system, before
	 trying to log in into the application you need to visit the Keycloak URL first and accept the
	 certificate. Otherwise, the login will fail.
4. `esthesis-core-srv-kubernetes` needs to be able to list all namespaces as well as schedule pods
	 via deployments, configure HPA, etc. A Service Account `esthesis-core-srv-kubernetes` is
	 automatically
	 created and configured with the necessary permissions. If you do not have the necessary
	 permissions
	 to properly configure this Service Account during the installation of the Helm chart, you can
	 disable the automatic creation of the Service Account by setting the
	 `esthesisKubernetesServiceCreateRBAC` variable from esthesis-core helm chart to `false` and
	 create the
	 Service Account manually. The resources that need to be manually created can be found in
	 [esthesis-core-srv-kubernetes](https://github.com/esthesis-iot/esthesis-helm/tree/main/esthesis-core/templates/srv-kubernetes/rbac)
	 and need to be available and properly configured before the
	 installation of the Helm chart.

## Cert Manager integration

If you have [Cert Manager](https://cert-manager.io) installed in your cluster, you can use it to
automatically generate and renew certificates for esthesis UI and Keycloak. To do so, you need to
set the following environmental variables:

```
esthesisUiCertManagerClusterIssuer: "letsencrypt-prod"
```

If you are using namespace scoped issuers, you can alternatively specify:

```
esthesisUiCertManagerIssuer: "letsencrypt-prod"
```

Make sure you specify only one of the two variants, otherwise the installation will fail. Do not
forget to change `letsencrypt-prod` to the value of your own issuer.

## Wildcard Certificate integration

If you have a wildcard certificate installed on you cluster as a secret for your domain, and you
want the included ingress-nginx controller to use it, you will need to set the following
environmental variables:

```
ingress-nginx.controller.service.externalIPs: "192.168.1.60"
```

Do not forget to change `192.168.1.60` to your external load balancer IP and `wildcard-tls` to the
name of your secret.
