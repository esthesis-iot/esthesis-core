# Developing backend services

esthesis CORE backend services provide the backend functionality for the esthesis CORE application. 
The backend services are developed using the Quarkus framework and follow the concept of microservices. Backend services
are defined into their own Maven module, and create a container image that can be deployed to Kubernetes.

Creating the business logic implementation of a backend service is a complex task that requires a good understanding of
the domain model and the requirements of the service. In addition to the Java source code, the backend services also 
needs to be properly integrated into the Helm charts.

The following sections present the steps required to create a new backend service, and how to integrate it into the
esthesis CORE Helm charts.

<tip>
Before creating a new backend service, it is strongly advised to review how existing services are structured.
</tip>

## New Maven module
A new backend service is created as a new Maven module. The module should be created in the `services` directory of the
`esthesis-core-backend` project. The module should follow the naming convention of `srv-{service-name}`, and consists
of an API and an implementation module, following the naming convention of `srv-{service-name}-client` and 
`srv-{service-name}-impl`, respectively.

The parent Maven module, should have as a parent the `esthesis-core-services` module.

## Service startup script
All backend services have a shell script, allowing you to start the service in development mode. The script is named
`dev-{service-name}.sh` and is located in the root of the service module. This script takes care of setting up the 
necessary environment for the service to run, and starts the service in development mode.

Make sure you configure any environment options related to the new service, and specify unique port numbers for the
service to run on.

### Local-only configuration
If you need to configure environment variables that are specific to your local development environment, you can create
a `local-env.sh` script alongside the `dev-{service-name}.sh` script. This script will be sourced by the
`dev-{service-name}.sh` script, and can be used to set environment variables that are specific to your local
development environment. The `local-env.sh` script should not be committed to the repository.

## Dev environment startup script
esthesis CORE provides a `tmux` script that allows you to start all backend services in a single terminal window. If you
create a new backend service, you need to update the `tmux-dev.sh` script to include the new service. The script is
located in the `_dev` directory of the `esthesis-core-backend` project.

The new service should be added in the `services` array under `## Start services` section of the script.

## Update Angular proxy configuration
The Angular frontend uses a proxy configuration to forward requests to the backend services. If you create a new backend
service, you need to update the `proxy.conf.json` file in the `esthesis-core-ui` project to include the new service.

[TBC]

note: mention dev profiles and how to use dev-.sh scripts

note: mention TESTCONTAINERS_RYUK_DISABLED=true
