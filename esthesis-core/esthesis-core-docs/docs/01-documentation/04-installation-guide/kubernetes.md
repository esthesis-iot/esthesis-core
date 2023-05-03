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
- A Kubernetes cluster with a minimum of 3 nodes.
- [Helm 3](https://helm.sh)
- [Helmfile](https://github.com/helmfile/helmfile)

## Configuration parameters
The following parameters can be defined as environmental variables during installation:

### General
🔹 `TIMEZONE`\
The containers timezone to set (note, some containers do not respect this setting).\
Default: `Europe/Athens`

🔹 `ESTHESIS_LOG_LEVEL`\
The log level to be used for the esthesis components (i.e. does not affect third-party components
installed by the Helm chart).\
Default: `WARN`

### Accounts
🔹 `ESTHESIS_ADMIN_USERNAME`\
The username of the esthesis administrator user. Use this account to connect to esthesis UI after installation is done.\
Default: `esthesis-admin`

🔹 `ESTHESIS_ADMIN_PASSWORD`\
The password of the esthesis administrator user.\
Default: `esthesis-admin`

🔹 `ESTHESIS_SYSTEM_USERNAME`\
The username of the esthesis system user. This is the user being used for esthesis inter-component
communication, as well as the default username for all other third-party products installed by the
Helm charts.\
Default: `esthesis-system`

🔹 `ESTHESIS_SYSTEM_PASSWORD`\
The password of the esthesis system user.\
Default: `esthesis-system`

### Keycloak
🔹 `KEYCLOAK_ENABLED`\
Whether Keycloak should be installed by this chart or not.\
Default: `true`

🔹 `KEYCLOAK_INGRESS_HOSTNAME`\
The hostname of the ingress rule that will be created for Keycloak\
Default: `keycloak.esthesis.local`

### MongoDB
🔹 `MONGODB_ENABLED`\
Whether MongoDB should be installed by this chart or not.\
Default: `true`

🔹 `MONGODB_URL_CLUSTER`\
The internal URL cluster components should use to connect to MongoDB.\
Default: `mongodb://mongodb:27017`

🔹 `MONGODB_DATABASE`\
The database name to use.\
Default: `esthesiscore`

🔹 `MONGODB_USERNAME`\
The username to authenticate with.\
Default: As specified in `ESTHESIS_SYSTEM_USERNAME`

🔹 `MONGODB_PASSWORD`\
The password to authenticate with.\
Default: As specified in `ESTHESIS_SYSTEM_PASSWORD`

### APISIX
🔹 `APISIX_ENABLED`\
Whether APISIX should be installed by this chart or not.\
Default: `true`

APISIX_INGRESS_NAMESPACE

### OpenID Connect
🔹 `OIDC_AUTHORITY_URL_EXTERNAL`\
The URL of the OpenID Connect authority to use for external connections. This URL should be accessible
from the end-user's Internet browser using esthesis UI.\
Default: `https://keycloak.esthesis.local/realms/esthesis`

🔹 `OIDC_AUTHORITY_URL_CLUSTER`\
The URL of the OpenID Connect authority to use for internal connections. This URL should be accessible
from components running inside the Kubernetes cluster.\
Default: `http://keycloak/realms/esthesis`

🔹 `OIDC_DISCOVERY_URL_CLUSTER`\
The URL of the OpenID Connect discovery endpoint to use for internal connections. This URL should be
accessible from components running inside the Kubernetes cluster.\
Default: `http://keycloak/realms/esthesis/.well-known/openid-configuration`

🔹 `OIDC_JWT_VERIFY_LOCATION_CLUSTER`\
The URL of the OpenID Connect JWT verification endpoint to use for internal connections. This URL
should be accessible from components running inside the Kubernetes cluster.\
Default: `http://keycloak/realms/esthesis/protocol/openid-connect/certs`

### esthesis UI
🔹 `ESTHESIS_UI_INGRESS_HOSTNAME`\
The hostname of the ingress rule that will be created for esthesis UI.\
Default: `esthesiscore.esthesis.local`

🔹 `ESTHESIS_UI_LOGOUT_URL`\
The URL to redirect to after logging out from esthesis UI.\
Default: `/logout`

### Redis
🔹 `REDIS_ENABLED`\
Whether Redis should be installed by this chart or not.\
Default: `true`

🔹 `REDIS_HOSTS`\
The list of Redis hosts to use. This URL should be accessible from components running inside the
Kubernetes cluster.\
Default: `redis-master:6379/0`

### RabbitMQ
🔹 `RABBITMQ_ENABLED`\
Whether RabbitMQ should be installed by this chart or not.\
Default: `true`

🔹 `rabbitmqErlangCookie`\
The Erlang cookie to use for RabbitMQ.\
Default: `esthesis`

### Kafka
🔹 `KAFKA_ENABLED`\
Whether Kafka should be installed by this chart or not.\
Default: `true`

🔹 `KAFKA_BOOTSTRAP_SERVERS`\
The list of Kafka bootstrap servers to use. This URL should be accessible from components running
inside the Kubernetes cluster.\
Default: `kafka:9092`

### Camunda
🔹 `CAMUNDA_ENABLED`\
Whether Camunda should be installed by this chart or not.\
Default: `true`

🔹 `CAMUNDA_GATEWAY_URL_CLUSTER`\
The URL of the Camunda gateway to use for internal connections. This URL should be accessible from
components running inside the Kubernetes cluster.\
Default: `camunda-zeebe-gateway:26500`

## Examples
### Microk8s
#### Additional configuration parameters
🔹 `MK8S_EXPOSE_INGRESS`\
Exposes the default ingress (NGINX) by creating a LoadBalancer type service.\
Default: `false`

🔹 `MK8S_INGRESS_NAMESPACE`\
The namespace to use for the default ingress (NGINX).\
Default: `ingress`

#### Installation example
```
DOMAIN=esthesis-prod.mydomain.com \
HELMFILE_DEV_MODE=true \
KEYCLOAK_INGRESS_HOSTNAME=keycloak.$DOMAIN  \
ESTHESIS_UI_INGRESS_HOSTNAME=esthesis-core.$DOMAIN  \
OIDC_AUTHORITY_URL_EXTERNAL="https://$KEYCLOAK_INGRESS_HOSTNAME/realms/esthesis"  \
OIDC_AUTHORITY_URL_CLUSTER="http://keycloak/realms/esthesis"  \
OIDC_DISCOVERY_URL_CLUSTER="http://keycloak/realms/esthesis/.well-known/openid-configuration"  \
OIDC_JWT_VERIFY_LOCATION_CLUSTER="http://keycloak/realms/esthesis/protocol/openid-connect/certs" \
MK8S_EXPOSE_INGRESS=true \
helmfile apply
```

> ⚠️ **Exposing the service**
>
> The entrypoint of the application is the `esthesis-core-ui-service` service which is an NGINX
> container. This container hosts the frontend of the application but also reverse proxies to the
> API Gateway of the application. You need to expose the HTTP endpoint of this service  in your
> TLS-terminating Load Balancer under HTTPS. Please note the application **will not** work under
> HTTP.

> ℹ️ **Accessing the application**
>
> - If you have installed the embedded Keycloak instance, and you have not modified the passwords
> above, you can access esthesis CORE with:\
`Username: esthesis-admin`\
`Password: esthesis`
> - If you are using self-signed certificates, you first need to access your Keycloak URL and trust
> it in your browser (or trust the self-signed certificate in your system's keystore). You can then
> open the application URL. This is only useful to quickly smoke test the installation and proper
> certificates need to be installed before opening the application to the public.

### Keycloak

Keycloak is an open source identity and access management solution. It is used by esthesis CORE to
authenticate users as esthesis CORE does not maintain an internal user database for authentication.
Note that esthesis CORE does maintain a user database but this is only used for authorisation
purposes (i.e. what each user can or can nor do in the system).

#### Disabling

If you want to disable the installation of Keycloak you can do so by:\
`--set keycloak.enabled=false`

#### Bring your own identity and access management

You can replace the embedded Keycloak instance with your own identity and access management solution
as long as it supports the OpenID Connect protocol. You will need to create the following resources:

- An `esthesis` realm [TBC]
- An `esthesis` client [TBC]
- Camunda client??? [TBC]
- An `ethesis-admin` and `ethesis-system` users [TBC]

#### Configuration

[Keycloak packaged by Bitnami](https://github.com/bitnami/charts/tree/main/bitnami/keycloak)

#### Tracing

[TBC]

#### Logging

[TBC]

#### Metrics

[TBC]

### APISIX

Apache APISIX is an open source, dynamic, scalable, and high-performance cloud native API gateway.
APISIX facilitates interface traffic handling for websites, mobile and IoT applications by providing
services such as load balancing, dynamic upstream, canary release, fine-grained routing, rate
limiting, and many more.

esthesis CORE uses APISIX as an API gateway as well the central point for authentication using
OpenID Connect.

#### Disabling

If you want to disable the installation of APISIX you can do so by:\
`--set apisix.enabled=false`

#### Bring your own API gateway

You can replace the embedded APISIX instance with your own API gateway as long as it supports the
OpenID Connect protocol. In that case you will be responsible to create all necessary routes to the
various microservices of the system as well as protecting those routes with OpenID Connect.

#### Configuration

[Apache APISIX for Kubernetes](https://github.com/apache/apisix-helm-chart/tree/master/charts/apisix)

#### Tracing

You can enable OpenTelemetry tracing for APISIX by setting:

```
--set apisix.pluginAttrs.opentelemetry.resource.service.name=apisix
--set apisix.pluginAttrs.opentelemetry.collector.address={your-collector}
```

#### Logging

[TBC]

#### Metrics

[TBC]
