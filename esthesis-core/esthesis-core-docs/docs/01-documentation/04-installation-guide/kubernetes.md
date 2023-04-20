# Kubernetes

esthesis CORE can be deployed on Kubernetes using the publicly available Helm charts. The Helm
charts
are available on the [TBC].

esthesis CORE comes with a variety of different Helm charts. Some of the provided Helm charts
pertain
to mandatory components, while others are optional. During the installation you can choose which
components you want to install by enabling the relevant configuration options. You can also choose
to use already existing resources, such as a database or a message broker, instead of the
ones provided by the Helm charts.

Please note that the default Helm charts come with reasonable defaults; we strongly advise to
review them, so you can customize them to your needs.

## Requirements
- A Kubernetes cluster with a minimum of 3 nodes. [TBC]
- An Ingress controller available in your Kubernetes cluster. [TBC]
- A terminating TLS Load Balancer. [TBC]

## Installation

You can install esthesis CORE using the `esthesis-core` Helm chart as:

```shell
helm repo add esthesis https://charts.esthes.is
helm install esthesis-core esthesis/esthesis-core
```

> ⚠️ **Keycloak config**
>
> Due to technical limitations, if you opt to use a different release name, you need to specify
> the following value:\
> `--set keycloak.keycloakConfigCli.existingConfigMap={YOUR-RELEASE-NAME}-keycloak-config`

[TBC] - Can we catch this in Helm an abort the deployment with a message?

> ⚠️ **Changing passwords**
>
> The above chart will install all required components for esthesis CORE using default values
> that you should modify before allowing external access to your installation, especially account
> passwords. Unfortunately, there is no easy way to define a global username/password pair and
> have that propagate to the third-party Helm subcharts we use (unless using tools external to
> Helm), so for
> convenience here is the list of all possible account names and passwords you can (should) change:
>
> ```shell
> NAMESPACE=esthesis-prod && \
> RELEASE_NAME=esthesis-core && \
> THIRD_PARTY_ADMIN_USERNAME=kostas && \
> THIRD_PARTY_ADMIN_PASSWORD=kostas && \
> ESTHESIS_ADMIN_USERNAME=kostas-admin && \
> ESTHESIS_ADMIN_PASSWORD=kostas && \
> ESTHESIS_SYSTEM_USERNAME=kostas-system && \
> ESTHESIS_SYSTEM_PASSWORD=kostas && \
> DOMAIN=$RELEASE_NAME.local && \
> helm -n $NAMESPACE upgrade --install $RELEASE_NAME . \
> 	--set esthesis.auth.adminUsername="$ESTHESIS_ADMIN_USERNAME" \
> 	--set esthesis.auth.adminPassword="$ESTHESIS_ADMIN_PASSWORD" \
> 	--set esthesis.auth.systemUsername="$ESTHESIS_SYSTEM_USERNAME" \
> 	--set esthesis.auth.systemPassword="$ESTHESIS_SYSTEM_PASSWORD" \
> 	--set esthesis.auth.thirdParty.adminUsername="$THIRD_PARTY_ADMIN_USERNAME" \
> 	--set esthesis.auth.thirdParty.adminPassword="$THIRD_PARTY_ADMIN_PASSWORD" \
> 	--set esthesis.ui.ingress.hostname="ui.$DOMAIN" \
> 	--set keycloak.auth.adminUser="$THIRD_PARTY_ADMIN_USERNAME" \
> 	--set keycloak.auth.adminPassword="$THIRD_PARTY_ADMIN_PASSWORD" \
> 	--set keycloak.auth.managementPassword="$THIRD_PARTY_ADMIN_PASSWORD" \
> 	--set keycloak.postgresql.auth.postgresPassword="$THIRD_PARTY_ADMIN_PASSWORD" \
> 	--set keycloak.postgresql.auth.password="$THIRD_PARTY_ADMIN_PASSWORD" \
>		--set keycloak.ingress.hostname="keycloak.$DOMAIN" \
> 	--set mongodb.auth.rootUser="$THIRD_PARTY_ADMIN_USERNAME" \
> 	--set mongodb.auth.rootPassword="$THIRD_PARTY_ADMIN_PASSWORD" \
> 	--set "mongodb.auth.usernames[0]"="$ESTHESIS_SYSTEM_USERNAME" \
> 	--set "mongodb.auth.passwords[0]"="$ESTHESIS_SYSTEM_PASSWORD" \
> 	--set mongodb.initdbScriptsConfigMap="$RELEASE_NAME-mongodb-init-cm" \
> 	--set apisix.admin.credentials.admin="$THIRD_PARTY_ADMIN_PASSWORD" \
> 	--set apisix.admin.credentials.viewer="$THIRD_PARTY_ADMIN_USERNAME" \
>		--set apisix.ingress-controller.config.apisix.serviceFullname="$RELEASE_NAME-apisix-admin" \
> 	--set apisix.ingress-controller.config.apisix.adminKey="$THIRD_PARTY_ADMIN_PASSWORD" \
> 	--set "apisix.dashboard.config.conf.etc.endpoints[0]"="$RELEASE_NAME-etc:2379" \
> 	--set apisix.dashboard.config.conf.authentication.secret="$THIRD_PARTY_ADMIN_PASSWORD" \
> 	--set "apisix.dashboard.config.authentication.users[0].username"="$THIRD_PARTY_ADMIN_USERNAME" \
> 	--set "apisix.dashboard.config.authentication.users[0].password"="$THIRD_PARTY_ADMIN_PASSWORD"
> ```

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
