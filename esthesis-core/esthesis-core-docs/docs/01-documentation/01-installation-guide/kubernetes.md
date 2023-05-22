# Kubernetes

esthesis CORE can be deployed on Kubernetes using the publicly available Helm charts. The Helm
charts are available on the [TBC].

esthesis CORE comes with a variety of different Helm charts. Some of the provided Helm charts
pertain to mandatory components, while others are optional. During the installation you can choose which
components you want to install by enabling the relevant configuration options. You can also choose
to use already existing resources, such as a database or a message broker, instead of the
ones provided in the Helm charts.

Please note that Helm charts come with reasonable defaults; we strongly advise to
review them, so you can customize them to your needs.

## Requirements
- A Kubernetes cluster with a minimum of 3 nodes and support for Load Balancer service types as well
as Ingress support.
- [Helm](https://helm.sh)
- [Helmfile](https://github.com/helmfile/helmfile)

## Configuration parameters
<details><summary>The following parameters can be defined as environmental variables during installation:</summary>

### General
🔹 `TIMEZONE`<br/>
The containers timezone to set (note, some containers do not respect this setting).<br/>
Default: `Europe/Athens`

🔹 `ESTHESIS_LOG_LEVEL`<br/>
The log level to be used for the esthesis components (i.e. does not affect third-party components
installed by the Helm chart).<br/>
Default: `WARN`

### Accounts
🔹 `ESTHESIS_ADMIN_USERNAME`<br/>
The username of the esthesis administrator user. Use this account to connect to esthesis UI after installation is done.<br/>
Default: `esthesis-admin`

🔹 `ESTHESIS_ADMIN_PASSWORD`<br/>
The password of the esthesis administrator user.<br/>
Default: `esthesis-admin`

🔹 `ESTHESIS_SYSTEM_USERNAME`<br/>
The username of the esthesis system user. This is the user being used for esthesis inter-component
communication, as well as the default username for all other third-party products installed by the
Helm charts.<br/>
Default: `esthesis-system`

🔹 `ESTHESIS_SYSTEM_PASSWORD`<br/>
The password of the esthesis system user.<br/>
Default: `esthesis-system`

### Keycloak
🔹 `KEYCLOAK_ENABLED`<br/>
Whether Keycloak should be installed by this chart or not.<br/>
Default: `true`

🔹 `KEYCLOAK_INGRESS_HOSTNAME`<br/>
The hostname of the ingress rule that will be created for Keycloak\
Default: `keycloak.esthesis.local`

### MongoDB
🔹 `MONGODB_ENABLED`<br/>
Whether MongoDB should be installed by this chart or not.<br/>
Default: `true`

🔹 `MONGODB_URL_CLUSTER`<br/>
The internal URL cluster components should use to connect to MongoDB.<br/>
Default: `mongodb://mongodb:27017`

🔹 `MONGODB_DATABASE`<br/>
The database name to use.<br/>
Default: `esthesiscore`

🔹 `MONGODB_USERNAME`<br/>
The username to authenticate with.<br/>
Default: As specified in `ESTHESIS_SYSTEM_USERNAME`

🔹 `MONGODB_PASSWORD`<br/>
The password to authenticate with.<br/>
Default: As specified in `ESTHESIS_SYSTEM_PASSWORD`

### APISIX
🔹 `APISIX_ENABLED`<br/>
Whether APISIX should be installed by this chart or not.<br/>
Default: `true`

🔹 `APISIX_INGRESS_NAMESPACE`<br/>
The namespace to monitor for ingress rules.<br/>
Default: (empty, all namespaces are monitored)

### OpenID Connect
🔹 `OIDC_AUTHORITY_URL_EXTERNAL`<br/>
The URL of the OpenID Connect authority to use for external connections. This URL should be accessible
from the end-user's Internet browser using esthesis UI.<br/>
Default: `https://keycloak.esthesis.local/realms/esthesis`

🔹 `OIDC_AUTHORITY_URL_CLUSTER`<br/>
The URL of the OpenID Connect authority to use for internal connections. This URL should be accessible
from components running inside the Kubernetes cluster.<br/>
Default: `http://keycloak/realms/esthesis`

🔹 `OIDC_DISCOVERY_URL_CLUSTER`<br/>
The URL of the OpenID Connect discovery endpoint to use for internal connections. This URL should be
accessible from components running inside the Kubernetes cluster.<br/>
Default: `http://keycloak/realms/esthesis/.well-known/openid-configuration`

🔹 `OIDC_JWT_VERIFY_LOCATION_CLUSTER`<br/>
The URL of the OpenID Connect JWT verification endpoint to use for internal connections. This URL
should be accessible from components running inside the Kubernetes cluster.<br/>
Default: `http://keycloak/realms/esthesis/protocol/openid-connect/certs`

### esthesis UI
🔹 `ESTHESIS_UI_INGRESS_HOSTNAME`<br/>
The hostname of the ingress rule that will be created for esthesis UI.<br/>
Default: `esthesiscore.esthesis.local`

🔹 `ESTHESIS_UI_LOGOUT_URL`<br/>
The URL to redirect to after logging out from esthesis UI.<br/>
Default: `/logout`

### Redis
🔹 `REDIS_ENABLED`<br/>
Whether Redis should be installed by this chart or not.<br/>
Default: `true`

🔹 `REDIS_HOSTS`<br/>
The list of Redis hosts to use. This URL should be accessible from components running inside the
Kubernetes cluster.<br/>
Default: `redis-master:6379/0`

### Mosquitto
🔹 `MOSQUITTO_ENABLED`<br/>
Whether Mosquitto should be installed by this chart or not.<br/>
Default: `true`

### Kafka
🔹 `KAFKA_ENABLED`<br/>
Whether Kafka should be installed by this chart or not.<br/>
Default: `true`

🔹 `KAFKA_BOOTSTRAP_SERVERS`<br/>
The list of Kafka bootstrap servers to use. This URL should be accessible from components running
inside the Kubernetes cluster.<br/>
Default: `kafka:9092`

### Camunda
🔹 `CAMUNDA_ENABLED`<br/>
Whether Camunda should be installed by this chart or not.<br/>
Default: `true`

🔹 `CAMUNDA_GATEWAY_URL_CLUSTER`<br/>
The URL of the Camunda gateway to use for internal connections. This URL should be accessible from
components running inside the Kubernetes cluster.<br/>
Default: `camunda-zeebe-gateway:26500`

### Microk8s
🔹 `MK8S_EXPOSE_INGRESS`<br/>
Exposes the default ingress (NGINX) by creating a LoadBalancer type service.<br/>
Default: `false`

🔹 `MK8S_INGRESS_NAMESPACE`<br/>
The namespace to use for the default ingress (NGINX).<br/>
Default: `ingress`
</details>

## Installation
esthesis Core comes in two Helm charts, one installing all the required dependencies and another one
installing the application components. You can enable/disable which specific dependencies you want
to install by setting the corresponding `*_ENABLED` parameter to `true` or `false`. Do note that
although the provided dependencies are adequate to have esthesis Core up and running, you might want to
tune their properties or replace them altogether with your own resources to support your specific
production use case.

### Environment variables
The following environment variables are used by the Helm charts to configure the installation, you
may change them to suit your needs:
```
export DOMAIN=esthesis-prod.home.nassosmichas.com
export TIMEZONE=Europe/Athens
export ESTHESIS_ADMIN_USERNAME=admin
export ESTHESIS_ADMIN_PASSWORD=admin
export ESTHESIS_SYSTEM_USERNAME=system
export ESTHESIS_SYSTEM_PASSWORD=system
export KEYCLOAK_INGRESS_HOSTNAME=keycloak.$DOMAIN
export ESTHESIS_UI_INGRESS_HOSTNAME=esthesis-core.$DOMAIN
export ESTHESIS_API_INGRESS_HOSTNAME=esthesis-core.$DOMAIN
export OIDC_AUTHORITY_URL_EXTERNAL="https://$KEYCLOAK_INGRESS_HOSTNAME/realms/esthesis"
```

### Supporting infrastructure
- Obtain the Helmfile corresponding to the esthesis version you want to install. For example:
	```shell
	wget https://esthes.is/helm/helmfile-esthesis-core-deps-3.0.0.tgz | tar xcfz
	```
- Install the Helmfile:
	```shell
	helmfile -f helmfile-esthesis-core-deps.yaml sync
	```

### Application
- Obtain the Helmfile corresponding to the esthesis version you want to install. For example:
	```shell
	wget https://esthes.is/helm/helmfile-esthesis-core-3.0.0.tgz
	```
- Install the Helmfile:
	```shell
	helmfile -f helmfile-esthesis-core.yaml sync
	```

## Notes
1. You need to access the UI via HTTPS, accessing it via HTTP will not work.
2. The UI is exposed under the domain you specified in the environmental variable `ESTHESIS_UI_INGRESS_HOSTNAME`.
3. If you are using a self-signed certificate which is not imported into your local system, before
trying to log in into the application you need to visit the Keycloak URL first and accept the
certificate. Otherwise, the login will fail.
