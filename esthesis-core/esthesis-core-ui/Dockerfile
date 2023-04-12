# syntax=docker/dockerfile:1
# You need DOCKER_BUILDKIT=1 to build this Dockerfile.

####################################################################################################
# Docker multistage build for esthesis CORE UI.
####################################################################################################
ARG ALPINE_VERSION=3.17.3
ARG NGINX_VERSION=1.23.4

####################################################################################################
# Create a self-signed certificate
####################################################################################################
FROM alpine:${ALPINE_VERSION} as alpine
ARG DOMAIN_NAME=localhost
ARG DAYS_VALID=3650

RUN apk add --no-cache openssl
RUN echo "Creating self-signed certificate valid for ${DAYS_VALID} days for domain ${DOMAIN_NAME}" && \
    openssl \
    req -x509 \
    -nodes \
    -subj "/CN=${DOMAIN_NAME}}" \
    -addext "subjectAltName=DNS:${DOMAIN_NAME}" \
    -days ${DAYS_VALID} \
    -newkey rsa:2048 -keyout /tmp/self-signed.key \
    -out /tmp/self-signed.crt

####################################################################################################
# Cache NPM modules.
####################################################################################################
FROM node:18.14.0-alpine as npm
LABEL maintainer="esthesis@eurodyn.com"

# Set working directory
WORKDIR /app

# Install and cache app dependencies
COPY package.json /app/package.json
RUN npm install

#####################################################################################################
## Build application.
#####################################################################################################
FROM node:18.14.0-alpine as build
LABEL maintainer="esthesis@eurodyn.com"

# Set working directory
WORKDIR /app

# Install and cache app dependencies
COPY --from=npm /app/node_modules /app/node_modules
COPY --from=npm /app/package.json /app/package.json
RUN npm install -g @angular/cli

# Add app
COPY . /app

# Generate build
RUN ng build --configuration production --output-path=dist

#####################################################################################################
## Create runtime container.
#####################################################################################################
FROM nginx:1.23.4
LABEL maintainer="esthesis@eurodyn.com"

# Copy self-signed certificate.
COPY --from=alpine /tmp/self-signed.key /etc/ssl/private
COPY --from=alpine /tmp/self-signed.crt /etc/ssl/certs

# Copy artifact build from the 'build environment'.
COPY --from=build /app/dist /usr/share/nginx/html

# Expose port 80
EXPOSE 80
