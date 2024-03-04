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
  as Ingress support (you can, optionally, install an nginx ingress controller using this chart)..
- [Helm](https://helm.sh)
- [Helmfile](https://github.com/helmfile/helmfile)

:::tip
If you are deploying esthesis CORE in a public cloud, make sue you also take into account any
specific instructions for the cloud provider you are using.
:::

## Configuration parameters

<details><summary>The following parameters can be defined as environmental variables during installation:</summary>

### General

🔹 `TIMEZONE`<br/>
The containers timezone to set (note, some containers do not respect this setting).<br/>
Default: `Europe/Athens`

🔹 `ESTHESIS_LOG_LEVEL`<br/>
The log level to be used for the esthesis components (i.e. does not affect third-party components installed by the Helm chart).<br/>
Default: `WARN`

🔹 `IMAGE_PULL_SECRET`<br/>
The secret to use when pulling container images.<br/>
Default: ``

🔹 `INGRESS_CLASS_NAME`<br/>
The name of the ingress class to use.<br/>
Default: ``

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

🔹 `ESTHESIS_SYSTEM_PASSWORD`<br/>
The password of the esthesis system user.<br/>
Default: `esthesis-system`

### Keycloak

🔹 `KEYCLOAK_ENABLED`<br/>
Whether Keycloak should be installed by this chart or not.<br/>
Default: `true`

🔹 `KEYCLOAK_HOSTNAME`<br/>
The hostname for Keycloak<br/>
Default: ``

🔹 `KEYCLOAK_CERT_MANAGER_CLUSTER_ISSUER`<br/>
The name of a Cert Manager cluster issuer to be used. This option is mutually exclusive with `KEYCLOAK_CERT_MANAGER_ISSUER`<br/>
Default: ``

🔹 `KEYCLOAK_CERT_MANAGER_ISSUER`<br/>
The name of a (namespace scoped) Cert Manager issuer to be used. This option is mutually exclusive
with `KEYCLOAK_CERT_MANAGER_CLUSTER_ISSUER`<br/>
Default: ``

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

### OpenID Connect

🔹 `OIDC_AUTHORITY_URL_EXTERNAL`<br/>
The URL of the OpenID Connect authority to use for external connections. This URL should be accessible
from the end-user's Internet browser using esthesis UI.<br/>
Default: ``

🔹 `OIDC_AUTHORITY_URL_CLUSTER`<br/>
The URL of the OpenID Connect authority to use for internal connections. This URL should be accessible
from components running inside the Kubernetes cluster.<br/>
Default: `http://keycloak.<Namespace>.svc.cluster.local/realms/esthesis`

🔹 `OIDC_DISCOVERY_URL_CLUSTER`<br/>
The URL of the OpenID Connect discovery endpoint to use for internal connections. This URL should be
accessible from components running inside the Kubernetes cluster.<br/>
Default: `http://keycloak.<Namespace>.svc.cluster.local/realms/esthesis/.well-known/openid-configuration`

🔹 `OIDC_JWT_VERIFY_LOCATION_CLUSTER`<br/>
The URL of the OpenID Connect JWT verification endpoint to use for internal connections. This URL
should be accessible from components running inside the Kubernetes cluster.<br/>
Default: `http://keycloak.<Namespace>.svc.cluster.local/realms/esthesis/protocol/openid-connect/certs`

### Ingress nginx

🔹 `INGRESS_NGINX_ENABLED`<br/>
Whether Ingress nginx should be installed by this chart or not.<br/>
Default: `false`

### esthesis UI

🔹 `ESTHESIS_HOSTNAME`<br/>
The hostname of the ingress rule that will be created for esthesis UI.<br/>
Default: ``

🔹 `ESTHESIS_UI_LOGOUT_URL`<br/>
The URL to redirect to after logging out from esthesis UI.<br/>
Default: `/logout`

🔹 `ESTHESIS_UI_CERT_MANAGER_CLUSTER_ISSUER`<br/>
The name of a Cert Manager cluster issuer to be used. This option is mutually exclusive with `ESTHESIS_UI_CERT_MANAGER_ISSUER`<br/>
Default: ``

🔹 `ESTHESIS_UI_CERT_MANAGER_ISSUER`<br/>
The name of a (namespace scoped) Cert Manager issuer to be used. This option is mutually exclusive
with `ESTHESIS_UI_CERT_MANAGER_CLUSTER_ISSUER`<br/>
Default: ``

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

🔹 `MOSQUITTO_MUTUAL_TLS`<br/>
Whether Mosquitto sohuld be configured for mutual TLS.<br/>
Default: `false`

🔹 `MOSQUITTO_SUPERUSER`<br/>
The name of the supe-user account. This account will be able to freely publish and subscribe to/from
any topic. When enabling TLS, this should be equal to the CN of the certificate.<br/>
Default: `esthesis`

🔹 `MOSQUITTO_CA_CERT`<br/>
The certificate of the CA, encoded in Base64.<br/>
Default: ``

🔹 `MOSQUITTO_SERVER_CERT`<br/>
The certificate of the mosquitto server, encoded in Base64.<br/>
Default: ``

🔹 `MOSQUITTO_SERVER_KEY`<br/>
The private key of the mosquitto server, encoded in Base64.<br/>
Default: ``

🔹 `MOSQUITTO_SERVICE_TYPE`<br/>
The type of the service to expose Mosquitto by.<br/>
Default: `ClusterIP`

### InfluxDB

🔹 `INFLUXDB_ENABLED`<br/>
Whether InfluxDB should be installed by this chart or not.<br/>
Default: `true`

🔹 `INFLUXDB_SIZE`<br/>
InfluxDB storage size.<br/>
Default: `32Gi`

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

</details>

## Installation

esthesis Core comes in two Helm charts, one installing all the required dependencies and another one
installing the application components. You can enable/disable which specific dependencies you want
to install by setting the corresponding `*_ENABLED` parameter to `true` or `false`. Do note that
although the provided dependencies are adequate to have esthesis Core up and running, you might want to
tune their properties or replace them altogether with your own resources to support your specific
production use case.

### Environment variables

The following list is a recommended starting point of environment variables to set before you
proceed with the installation:

```
export DOMAIN=domain.com
export TIMEZONE=Europe/Athens
export ESTHESIS_ADMIN_USERNAME=esthesis-admin
export ESTHESIS_ADMIN_PASSWORD=wEc25LP82F97OfoX
export ESTHESIS_SYSTEM_USERNAME=esthesis-system
export ESTHESIS_SYSTEM_PASSWORD=O0ddC1Qhf4cjW21N
export KEYCLOAK_INGRESS_NAME=keycloak.$DOMAIN
export ESTHESIS_INGRESS_NAME=esthesis-core.$DOMAIN
export OIDC_AUTHORITY_URL_EXTERNAL="https://$KEYCLOAK_INGRESS_NAME/realms/esthesis"
export KEYCLOAK_CERT_MANAGER_CLUSTER_ISSUER=letsencrypt-prod
export ESTHESIS_UI_CERT_MANAGER_CLUSTER_ISSUER=letsencrypt-prod
export MOSQUITTO_SERVICE_TYPE=LoadBalancer
```

Make sure you adapt the values to your own environment.

### Supporting infrastructure

- Obtain the Helmfile corresponding to the esthesis version you want to install. For example:
  ```shell
  wget -qO- https://esthes.is/helm/helmfile-esthesis-core-deps-3.0.27.tgz | tar xvz
  ```
- Install the Helmfile:
  ```shell
  helmfile sync --namespace={my-namespace}
  ```

### Application

- Obtain the Helmfile corresponding to the esthesis version you want to install. For example:
  ```shell
  wget -qO- https://esthes.is/helm/helmfile-esthesis-core-3.0.27.tgz | tar xvz
  ```
- Install the Helmfile:
  ```shell
  helmfile sync --namespace={my-namespace}
  ```

## Notes

1. You need to access the UI via HTTPS, accessing it via HTTP will not work.
2. The UI is exposed under the domain you specified in the environmental variable `ESTHESIS_UI_INGRESS_HOSTNAME`.
3. If you are using a self-signed certificate which is not imported into your local system, before
   trying to log in into the application you need to visit the Keycloak URL first and accept the
   certificate. Otherwise, the login will fail.
4. `esthesis-core-srv-kubernetes` needs to be able to list all namespaces as well as schedule pods
   via deployments, configure HPA, etc. A Service Account `esthesis-core-srv-kubernetes` is automatically
   created with no additional permissions other than the ones of the `default` service account. Depending
   on how security is implemented in your Kubernetes cluster, you may need to provide the necessary
   roles/permissions to this service account.

## Cert Manager integration

If you have [Cert Manager](https://cert-manager.io) installed in your cluster, you can use it to
automatically generate and renew certificates for esthesis UI and Keycloak. To do so, you need to
set the following environmental variables:

```
export KEYCLOAK_CERT_MANAGER_CLUSTER_ISSUER=letsencrypt-prod
export ESTHESIS_UI_CERT_MANAGER_CLUSTER_ISSUER=letsencrypt-prod
```

If you are using namespace scoped issuers, you can alternatively specify:

```
export KEYCLOAK_CERT_MANAGER_ISSUER=letsencrypt-prod
export ESTHESIS_UI_CERT_MANAGER_ISSUER=letsencrypt-prod
```

Make sure you specify only one of the two variants, otherwise the installation will fail. Do not
forget to change `letsencrypt-prod` to the value of your own issuer.
