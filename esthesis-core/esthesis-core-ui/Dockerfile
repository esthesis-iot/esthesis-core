# syntax=docker/dockerfile:1
# You need DOCKER_BUILDKIT=1 to build this Dockerfile.

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

# Copy artifact build from the 'build environment'.
COPY --from=build /app/dist /usr/share/nginx/html

# Patch nginx configuration to amend try_files.
RUN sed -i 's/location \/ {/location \/ {\n        try_files \$uri \$uri\/ \/index\.html \=404;\n/g' /etc/nginx/conf.d/default.conf

# Expose port 80
EXPOSE 80
