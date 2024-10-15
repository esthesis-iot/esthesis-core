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
  as Ingress support (you can, optionally, install an nginx ingress controller using this chart)..
- [Helm](https://helm.sh)
- [Helmfile](https://github.com/helmfile/helmfile)

<tip>
If you are deploying esthesis CORE in a public cloud, make sue you also take into account any
specific instructions for the cloud provider you are using.
</tip>

## Configuration parameters
The following parameters can be defined as environmental variables during installation:

<chapter title="General" id="general" collapsible="true">

`TIMEZONE`
: The containers timezone to set (note, some containers do not respect this setting).
<br/>Default: `Europe/Athens`

`ESTHESIS_LOG_LEVEL`
: The log level to be used for the esthesis components (i.e. does not affect third-party components installed by the Helm chart).
<br/>Default: `WARN`

`IMAGE_PULL_SECRET`
: The secret to use when pulling container images.

`INGRESS_CLASS_NAME`
: The name of the ingress class to use.

</chapter>

<chapter title="Accounts" id="accounts" collapsible="true">

`ESTHESIS_ADMIN_USERNAME`
: The username of the esthesis administrator user. Use this account to connect to esthesis UI after installation is done.
<br/>Default: `esthesis-admin`

`ESTHESIS_ADMIN_PASSWORD`
: The password of the esthesis administrator user.
<br/>Default: `esthesis-admin`

`ESTHESIS_SYSTEM_USERNAME`
: The username of the esthesis system user. This is the user being used for esthesis inter-component
communication, as well as the default username for all other third-party products installed by the
Helm charts.
<br/>Default: `esthesis-system`

`ESTHESIS_SYSTEM_PASSWORD`
: The password of the esthesis system user.
<br/>Default: `esthesis-system`

`ESTHESIS_SYSTEM_PASSWORD`
: The password of the esthesis system user.
<br/>Default: `esthesis-system`

`ESTHESIS_KUBERNETES_SERVICE_CREATE_RBAC`
: The Kubernetes microservice needs to be able to access the Kubernetes API to create resources. The
deployment can automatically create all necessary roles and bindings for it, however if you do not
have such permissions in your cluster you can disable it and create them manually.
<br/>Default: `true`

</chapter>

<chapter title="Keycloak" id="keycloak" collapsible="true">

`KEYCLOAK_ENABLED`
: Whether Keycloak should be installed by this chart or not.
<br/>Default: `true`

`KEYCLOAK_HOSTNAME`
: The hostname for Keycloak

`KEYCLOAK_CERT_MANAGER_CLUSTER_ISSUER`
: The name of a Cert Manager cluster issuer to be used. This option is mutually exclusive with `KEYCLOAK_CERT_MANAGER_ISSUER`

`KEYCLOAK_CERT_MANAGER_ISSUER`
: The name of a (namespace scoped) Cert Manager issuer to be used. This option is mutually exclusive
with `KEYCLOAK_CERT_MANAGER_CLUSTER_ISSUER`

</chapter>

<chapter title="MongoDB" id="mongodb" collapsible="true">

`MONGODB_ENABLED`
: Whether MongoDB should be installed by this chart or not.
<br/>Default: `true`

`MONGODB_URL_CLUSTER`
: The internal URL cluster components should use to connect to MongoDB.
<br/>Default: `mongodb://mongodb:27017`

`MONGODB_DATABASE`
: The database name to use.
<br/>Default: `esthesiscore`

`MONGODB_USERNAME`
: The username to authenticate with.
<br/>Default: As specified in `ESTHESIS_SYSTEM_USERNAME`

`MONGODB_PASSWORD`
: The password to authenticate with.
<br/>Default: As specified in `ESTHESIS_SYSTEM_PASSWORD`

</chapter>

<chapter title="OpenID Connect" id="oidc" collapsible="true">

`OIDC_AUTH_SERVER_URL`
: The URL of the OpenID Connect authority to use. This URL should be accessible for intra-service
communication, so it does not need to be accessible for end-users.
<br/>Default: http://keycloak._Namespace_.svc.cluster.local/realms/esthesis

`OIDC_CLIENT_AUTH_SERVER_URL`
: The URL of the OpenID Connect authority to use when a service needs to authenticate autonomously
(this is needed for services of type 'system', which do not act on behalf of a user). This URL
should be accessible for intra-service communication, so it does not need to be accessible for
end-users.
<br/>Default: http://keycloak._Namespace_.svc.cluster.local/realms/esthesis

`OIDC_TLS_VERIFICATION`
: Whether TLS should be verified when contacting OpenID Connect authority.
<br/>Default: `required`

`ESTHESIS_REPORTED_OIDC_AUTHORITY_URL`
: The URL of the OpenID Connect authority end-users use to authenticate.
<br/>Default: http://keycloak._Namespace_.svc.cluster.local/realms/esthesis

`ESTHESIS_REPORTED_OIDC_POST_LOGOUT_URL`
: The URL the user is forwarded to when logging out.
<br/>Default: http://esthesis-core._Namespace_.svc.cluster.local/logout

</chapter>

<chapter title="NGINX Ingress" id="ingnginx" collapsible="true">

`INGRESS_NGINX_ENABLED`
: Whether Ingress nginx should be installed by this chart or not.
<br/>Default: `false`

`INGRESS_NGINX_SSL_CERT_ARN`
: The arn of the certificate.

</chapter>

<chapter title="User Interface" id="ui" collapsible="true">

`ESTHESIS_HOSTNAME`
: The hostname of the ingress rule that will be created for esthesis UI.

`ESTHESIS_UI_LOGOUT_URL`
: The URL to redirect to after logging out from esthesis UI.
<br/>Default: `/logout`

`ESTHESIS_UI_CERT_MANAGER_CLUSTER_ISSUER`
: The name of a Cert Manager cluster issuer to be used. This option is mutually exclusive with `ESTHESIS_UI_CERT_MANAGER_ISSUER`

`ESTHESIS_UI_CERT_MANAGER_ISSUER`
: The name of a (namespace scoped) Cert Manager issuer to be used. This option is mutually exclusive
with `ESTHESIS_UI_CERT_MANAGER_CLUSTER_ISSUER`.

</chapter>

<chapter title="Redis" id="redis" collapsible="true">

`REDIS_ENABLED`
: Whether Redis should be installed by this chart or not.
<br/>Default: `true`

`REDIS_HOSTS`
: The list of Redis hosts to use. This URL should be accessible from components running inside the
Kubernetes cluster.
<br/>Default: `redis-master:6379/0`

</chapter>

<chapter title="Mosquitto" id="mosquitto" collapsible="true">

`MOSQUITTO_ENABLED`
: Whether Mosquitto should be installed by this chart or not.
<br/>Default: `true`

`MOSQUITTO_MUTUAL_TLS`
: Whether Mosquitto sohuld be configured for mutual TLS.
<br/>Default: `false`

`MOSQUITTO_SUPERUSER`
: The name of the supe-user account. This account will be able to freely publish and subscribe to/from
any topic. When enabling TLS, this should be equal to the CN of the certificate.
<br/>Default: `esthesis`

`MOSQUITTO_CA_CERT`
: The certificate of the CA, encoded in Base64.

`MOSQUITTO_SERVER_CERT`
: The certificate of the mosquitto server, encoded in Base64.

`MOSQUITTO_SERVER_KEY`
: The private key of the mosquitto server, encoded in Base64.

`MOSQUITTO_SERVICE_TYPE`
: The type of the service to expose Mosquitto by.
<br/>Default: `ClusterIP`

</chapter>

<chapter title="InfluxDB" id="influxdb" collapsible="true">

`INFLUXDB_ENABLED`
: Whether InfluxDB should be installed by this chart or not.
<br/>Default: `true`

`INFLUXDB_SIZE`
: InfluxDB storage size.
<br/>Default: `32Gi`

</chapter>

<chapter title="Kafka" id="kafka" collapsible="true">

`KAFKA_ENABLED`
: Whether Kafka should be installed by this chart or not.
<br/>Default: `true`

`KAFKA_BOOTSTRAP_SERVERS`
: The list of Kafka bootstrap servers to use. This URL should be accessible from components running
inside the Kubernetes cluster.
<br/>Default: `kafka:9092`

</chapter>

<chapter title="Camunda" id="camunda" collapsible="true">

`CAMUNDA_ENABLED`
: Whether Camunda should be installed by this chart or not.<br/>
<br/>Default: `true`

`CAMUNDA_GATEWAY_URL_CLUSTER`
: The URL of the Camunda gateway to use for internal connections. This URL should be accessible from
components running inside the Kubernetes cluster.
<br/>Default: `camunda-zeebe-gateway:26500`

</chapter>

## Installation
esthesis CORE comes in two Helm charts, one installing all the required dependencies and another one
installing the application components. You can enable/disable which specific dependencies you want
to install by setting the corresponding `*_ENABLED` parameter to `true` or `false`. Do note that
although the provided dependencies are adequate to have esthesis CORE up and running, you might want to
tune their properties or replace them altogether with your own resources to support your specific
production use case.

### Environment variables
The following list is a starting point of environment variables to set before you proceed with the
installation, you need to amend them to match your own environment:
```
export DOMAIN=esthesis.domain.com
export TIMEZONE=Europe/Athens
export ESTHESIS_ADMIN_USERNAME=esthesis-admin
export ESTHESIS_ADMIN_PASSWORD=esthesis-admin
export ESTHESIS_SYSTEM_USERNAME=esthesis-system
export ESTHESIS_SYSTEM_PASSWORD=esthesis-system
export KEYCLOAK_HOSTNAME=keycloak.$DOMAIN
export MOSQUITTO_SERVICE_TYPE=LoadBalancer
export ESTHESIS_HOSTNAME=esthesis-core.$DOMAIN
export ESTHESIS_REPORTED_OIDC_AUTHORITY_URL="https://$KEYCLOAK_HOSTNAME/realms/esthesis"
export ESTHESIS_REPORTED_OIDC_POST_LOGOUT_URL="https://$ESTHESIS_HOSTNAME/logged-out"
export OIDC_AUTH_SERVER_URL="https://$KEYCLOAK_HOSTNAME/realms/esthesis"
export OIDC_CLIENT_AUTH_SERVER_URL="https://$KEYCLOAK_HOSTNAME/realms/esthesis"
```

### Supporting infrastructure
- Obtain the Helmfile corresponding to the esthesis version you want to install. For example:
  ```shell
  wget -qO- https://esthes.is/helm/helmfile-esthesis-core-deps-3.0.54.tgz | tar xvz
  ```
- Install the Helmfile:
  ```shell
  helmfile sync --namespace={my-namespace}
  ```

### Application
- Obtain the Helmfile corresponding to the esthesis version you want to install. For example:
  ```shell
  wget -qO- https://esthes.is/helm/helmfile-esthesis-core-3.0.54.tgz | tar xvz
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
   created and configured with the necessary permissions. If you do not have the necessary permissions
	 to properly configure this Service Account during the installation of the Helm chart, you can
	 disable the automatic creation of the Service Account by setting the
	 `ESTHESIS_KUBERNETES_SERVICE_CREATE_RBAC` environmental variable to `false` and create the
	 Service Account manually. The resources that need to be manually created can be found in
	 [esthesis-core-srv-kubernetes](https://github.com/esthesis-iot/esthesis-helm/tree/main/esthesis-core/templates/srv-kubernetes/rbac) and need to be available and properly configured before the
	 installation of the Helm chart.

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

## Wildcard Certificate integration
If you have a wildcard certificate installed on you cluster as a secret for your domain, and you want the included ingress-nginx controller to use it, you will need to set the following environmental variables:

```
export INGRESS_NGINX_LOAD_BALANCER_IP=192.168.1.60
export INGRESS_NGINX_CUSTOM_SSL=wildcard-tls
```

Do not forget to change `192.168.1.60` to your external load balancer IP and `wildcard-tls` to the name of your secret.
