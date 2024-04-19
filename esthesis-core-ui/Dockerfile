####################################################################################################
# Cache NPM modules.
####################################################################################################
# node:lts-bookworm-slim
FROM node:lts-bookworm-slim as npm
LABEL maintainer="esthesis@eurodyn.com"

# Set working directory
WORKDIR /app

# Install and cache app dependencies
COPY package.json /app/package.json

RUN npm config set proxy null \
    && npm config set https-proxy null \
    && npm config set noproxy registry.npmjs.org \
    && npm install --no-audit --maxsockets 1

#####################################################################################################
## Build application.
#####################################################################################################
# node:lts-bookworm-slim
FROM node:lts-bookworm-slim as build
LABEL maintainer="esthesis@eurodyn.com"

# Set working directory
WORKDIR /app

# Install and cache app dependencies
COPY --from=npm /app/node_modules /app/node_modules
COPY --from=npm /app/package.json /app/package.json

RUN npm install -g @angular/cli --no-audit

# Add app
COPY . /app

# Generate build
RUN ng build --configuration production --output-path=dist

#####################################################################################################
## Create runtime container.
#####################################################################################################
# nginx:stable
FROM nginx:stable
LABEL maintainer="esthesis@eurodyn.com"

# Copy artifact build from the 'build environment'.
COPY --from=build /app/dist /usr/share/nginx/html

# Patch nginx configuration to amend try_files.
RUN sed -i 's/location \/ {/location \/ {\n        try_files \$uri \$uri\/ \/index\.html \=404;\n/g' /etc/nginx/conf.d/default.conf

# Expose port 80
EXPOSE 80
