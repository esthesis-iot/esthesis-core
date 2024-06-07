# Resources vs System Resources
This page explains the concept of Resources and System Resources in esthesis.

## Service module structure
Before we delve into the details, we should first have a basic understanding of the structure of an
esthesis service:
- Client module: The client module defines the client interface of the service. It is the only
	module that is exposed to the outside world, and it is the only module that can be
	imported by other services.
- Impl module: The impl module provides the implementation of the client module. This module should
never be imported by other services.

The client module of each service is kind of "special", as it is annotated with Quarkus'
`@RegisterRestClient` annotation. This annotation tells Quarkus to generate a REST client for the
service which can then be injected into other services. This way we get a "free" client for each
service that does not need any additional maintenance as the service evolves.

## Security
esthesis' security is based on OIDC. An end-user trying to access an esthesis service needs to
provide a valid access token. This token is then validated by the service against the OIDC provider.
The same applies to service-to-service communication: The source service needs to provide a valid
access token to the target service. This token is then validated by the target service against the
OIDC provider.

But what happens when a service needs to access another service without having an end-user initiating
the request thus providing an access token? This is where the concept of System Resources comes into
play.

Effectively, the differentiation of Resources and System Resources is only relevant to the underlying
security mechanism: Resources are services requiring an active end-user (i.e. a valid access token
provided by the user), whereas System Resources are services that do not require an active end-user.
At this point you may be wondering: "...and who provides the access token for System Resources?".
The following sections provide an overview of how Resources and System Resources are defined and used
in esthesis.

## Resource
### Source service
As the source service does not know the location of the target service merely by importing the target
service's REST client, you need to define the target service's location in the source service's
`application.properties` file. Here is an example of how this can be done:
```properties
rest-client:
	SettingsResource:
		url: http://esthesis-core-srv-settings-service:8080
		scope: Singleton
```
The above snippet defines the URL of the `SettingsResource` service. You can then inject the
`SettingsResource` REST client into your source service and use it to communicate with the Settings
service.

### Target service
- Annotate the `SettingsResource` interface with `@RegisterRestClient(configKey = "SettingsResource")`.
It is important to note that the value of the `configKey` parameter must match the name of the service
in source service's `application.properties` file.
- Annotate the `SettingsResource` interface with `@RegisterProvider(AccessTokenRequestReactiveFilter.class)`.
This annotation tells Quarkus to inject the `AccessTokenRequestReactiveFilter` into the REST client,
which is responsible for propagating the access token provided by the end-user calling the source
service.

## System Resource
### Source service
Similarly to Resources, you need to define the target service's location in the source service's
`application.properties` file. Here is an example of how this can be done:
```properties
rest-client:
	SettingsSystemResource:
		url: http://esthesis-core-srv-settings-service:8080
		scope: Singleton
```
The above snippet defines the URL of the `SettingsSystemResource` service. You can then inject the
`SettingsSystemResource` REST client into your source service and use it to communicate with the Settings
service.

In this case, the source service will act as an OIDC client, therefore it needs to be configured
appropriately to obtain an access token from the OIDC provider. This is done by adding the following
properties to the source service's `application.properties` file:
```properties
oidc-client:
	client-id: esthesis
	grant:
		type: password
	grant-options:
		password:
			username: ${OIDC_CLIENT_USERNAME}
			password: ${OIDC_CLIENT_PASSWORD}
```
`${OIDC_CLIENT_USERNAME}` and `${OIDC_CLIENT_USERNAME}` are environment variables allowing the OIDC
client to authenticate against the OIDC provider using a password grant. For development, you can
set those values to the default values used in esthesis' default Keycloak esthesis-realm
`esthesis-system`. In production, those values will be provided by the Kubernetes deployment descriptor
resulting from the esthesis Helm charts. To instruct Helm to inject those values into a deployment, you
need to set the following flags to the `deployment.yaml` descriptor of the source service:
```yaml
"podOidcClient" true
```

### Target service
- Annotate the `SettingsSystemResource` interface with `@RegisterRestClient(configKey = "SettingsSystemResource")`.
	It is important to note that the value of the `configKey` parameter must match the name of the service
	in source service's `application.properties` file.
- Annotate the `SettingsSystemResource` interface with `@RegisterProvider(OidcClientRequestReactiveFilter.class)`.
	This annotation tells Quarkus to inject the `OidcClientRequestReactiveFilter` into the REST client,
	which is responsible to obtain (e.g. create) a valid access token from the OIDC provider and propagate
	it to the target service. Using this annotation the source service acts as an OIDC client obtaining
	a new access token for itself which then uses to identifies itself to the target service. In esthesis'
	default Keycloak esthesis-realm, there is a user created especially for this purpose.
