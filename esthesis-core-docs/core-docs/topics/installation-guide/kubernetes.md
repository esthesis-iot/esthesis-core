# Kubernetes

Esthesis CORE can be deployed on Kubernetes using the publicly available Helm charts. The Helm
charts are available on [the Helm Chart Repo](https://esthes.is/helm).

Esthesis CORE comes with a variety of different Helm charts. Some of the provided Helm charts
pertain to mandatory components, while others are optional. During the installation you can choose which
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

`timezone`<br/>
: The container's timezone to set (note, some containers do not respect this setting).<br/>
Default: `Europe/Athens`<br/><br/>

`esthesisLogLevel`<br/>
: The log level to be used for the esthesis components (i.e. does not affect third-party components installed by the Helm chart).<br/>
Default: `WARN`<br/><br/>

`imagePullSecret`<br/>
: The name of the Kubernetes Secret to use when pulling container images.<br/><br/>

`ingressClassName`<br/>
: The name of the ingress class to use for ingress rules.<br/><br/>

</chapter>

<chapter title="Accounts" id="accounts" collapsible="true">

`esthesisAdminUsername`<br/>
: The username of the Esthesis administrator.<br/>
Default: `esthesis-admin`<br/><br/>

`esthesisAdminPassword`<br/>
: The password for the Esthesis administrator.<br/>
Default: `esthesis-admin`<br/><br/>

`esthesisSystemUsername`<br/>
: The system-level user for Esthesis services and third-party components.<br/>
Default: `esthesis-system`<br/><br/>

`esthesisSystemPassword`<br/>
: The password for the system-level user.<br/>
Default: `esthesis-system`<br/><br/>

`esthesisKubernetesServiceCreateRBAC`<br/>
: Whether to create Kubernetes RBAC resources automatically.<br/>
Default: `true`<br/><br/>

</chapter>

<chapter title="Keycloak" id="keycloak" collapsible="true">

`keycloak.enabled`<br/>
: Whether to deploy Keycloak.<br/>
Default: `true`<br/><br/>

`keycloak.ingress.hostname`<br/>
: The external hostname for Keycloak.<br/><br/>

`keycloak.certManager.clusterIssuer`<br/>
: Cluster-wide Cert Manager issuer name. Mutually exclusive with `keycloak.certManager.issuer`.<br/><br/>

`keycloak.certManager.issuer`<br/>
: Namespace-scoped Cert Manager issuer name. Mutually exclusive with `keycloak.certManager.clusterIssuer`.<br/><br/>

</chapter>

<chapter title="MongoDB" id="mongodb" collapsible="true">

`mongodb.enabled`<br/>
: Whether to deploy MongoDB.<br/>
Default: `true`<br/><br/>

`mongodb.urlCluster`<br/>
: Internal MongoDB connection URL for Esthesis components.<br/>
Default: `mongodb://mongodb:27017`<br/><br/>

`mongodb.database`<br/>
: Name of the MongoDB database.<br/>
Default: `esthesiscore`<br/><br/>

`mongodb.username`<br/>
: MongoDB user (usually inherits from `esthesisSystemUsername`).<br/><br/>

`mongodb.password`<br/>
: MongoDB password (usually inherits from `esthesisSystemPassword`).<br/><br/>

</chapter>

<chapter title="MongoDB" id="mongodb" collapsible="true">

`mongodb.enabled`<br/>
: Whether to deploy MongoDB.<br/>
Default: `true`<br/><br/>

`mongodb.urlCluster`<br/>
: Internal MongoDB connection URL for Esthesis components.<br/>
Default: `mongodb://mongodb:27017`<br/><br/>

`mongodb.database`<br/>
: Name of the MongoDB database.<br/>
Default: `esthesiscore`<br/><br/>

`mongodb.username`<br/>
: MongoDB user (usually inherits from `esthesisSystemUsername`).<br/><br/>

`mongodb.password`<br/>
: MongoDB password (usually inherits from `esthesisSystemPassword`).<br/><br/>

</chapter>

<chapter title="NGINX Ingress" id="ingnginx" collapsible="true">

`ingressNginx.enabled`<br/>
: Whether to install the nginx ingress controller.<br/>
Default: `false`<br/><br/>

`ingressNginx.sslCertArn`<br/>
: ARN of the wildcard certificate to use.<br/><br/>

</chapter>

<chapter title="User Interface" id="ui" collapsible="true">

`esthesisHostname`<br/>
: External hostname for Esthesis UI.<br/><br/>

`esthesisUi.logoutUrl`<br/>
: Path to redirect users to after logout.<br/>
Default: `/logout`<br/><br/>

`esthesisUi.certManager.clusterIssuer`<br/>
: Cert Manager cluster-wide issuer for UI TLS.<br/><br/>

`esthesisUi.certManager.issuer`<br/>
: Cert Manager namespace-scoped issuer for UI TLS.<br/><br/>

</chapter>

<chapter title="Redis" id="redis" collapsible="true">

`redis.enabled`<br/>
: Whether to deploy Redis.<br/>
Default: `true`<br/><br/>

`redis.hosts`<br/>
: List of Redis endpoints.<br/>
Default: `redis-master:6379/0`<br/><br/>

</chapter>

<chapter title="Mosquitto" id="mosquitto" collapsible="true">

`mosquitto.enabled`<br/>
: Whether to deploy Mosquitto.<br/>
Default: `true`<br/><br/>

`mosquitto.mutualTls`<br/>
: Enable mutual TLS for Mosquitto.<br/>
Default: `false`<br/><br/>

`mosquitto.superuser`<br/>
: Super-user account (should match certificate CN when TLS is enabled).<br/>
Default: `esthesis`<br/><br/>

`mosquitto.caCert`<br/>
: Base64-encoded CA certificate.<br/><br/>

`mosquitto.serverCert`<br/>
: Base64-encoded Mosquitto server certificate.<br/><br/>

`mosquitto.serverKey`<br/>
: Base64-encoded Mosquitto private key.<br/><br/>

`mosquitto.serviceType`<br/>
: Kubernetes service type to expose Mosquitto.<br/>
Default: `ClusterIP`<br/><br/>

</chapter>

<chapter title="InfluxDB" id="influxdb" collapsible="true">

`influxdb.enabled`<br/>
: Whether to deploy InfluxDB.<br/>
Default: `true`<br/><br/>

`influxdb.size`<br/>
: Persistent volume size for InfluxDB.<br/>
Default: `32Gi`<br/><br/>

</chapter>

<chapter title="Kafka" id="kafka" collapsible="true">

`kafka.enabled`<br/>
: Whether to deploy Kafka.<br/>
Default: `true`<br/><br/>

`kafka.bootstrapServers`<br/>
: List of Kafka bootstrap servers.<br/>
Default: `kafka:9092`<br/><br/>

</chapter>

<chapter title="Camunda" id="camunda" collapsible="true">

`camunda.enabled`<br/>
: Whether to deploy Camunda.<br/>
Default: `true`<br/><br/>

`camunda.gatewayUrlCluster`<br/>
: Internal gateway URL for Camunda Zeebe.<br/>
Default: `camunda-zeebe-gateway:26500`<br/><br/>

</chapter>

## Installation
esthesis CORE comes in two Helm charts, one installing all the required dependencies and another one
installing the application components. You can enable/disable which specific dependencies you want
to install by setting the corresponding `charts_enabled.<service>` parameter to `true` or `false`. Do note that
although the provided dependencies are adequate to have esthesis CORE up and running, you might want to
tune their properties or replace them altogether with your own resources to support your specific
production use case.

### Environment variables
The following list is a starting point of variables on values.yaml to set before you proceed on both helm charts with the installation, you need to amend them to match your own environment:

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
keycloak.ingress.hostname: "keycloak.esthesis.domain.com"
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
2. The UI is exposed under the domain you specified in the environmental variable `esthesisHostname`.
3. If you are using a self-signed certificate which is not imported into your local system, before
   trying to log in into the application you need to visit the Keycloak URL first and accept the
   certificate. Otherwise, the login will fail.
4. `esthesis-core-srv-kubernetes` needs to be able to list all namespaces as well as schedule pods
   via deployments, configure HPA, etc. A Service Account `esthesis-core-srv-kubernetes` is automatically
   created and configured with the necessary permissions. If you do not have the necessary permissions
	 to properly configure this Service Account during the installation of the Helm chart, you can
	 disable the automatic creation of the Service Account by setting the
	 `esthesisKubernetesServiceCreateRBAC` variable from esthesis-core helm chart to `false` and create the
	 Service Account manually. The resources that need to be manually created can be found in
	 [esthesis-core-srv-kubernetes](https://github.com/esthesis-iot/esthesis-helm/tree/main/esthesis-core/templates/srv-kubernetes/rbac) and need to be available and properly configured before the
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
If you have a wildcard certificate installed on you cluster as a secret for your domain, and you want the included ingress-nginx controller to use it, you will need to set the following environmental variables:

```
ingress-nginx.controller.service.externalIPs: "192.168.1.60"
```

Do not forget to change `192.168.1.60` to your external load balancer IP and `wildcard-tls` to the name of your secret.
