####################################################################################################
# Cache NPM modules.
####################################################################################################
# node:lts-bookworm-slim
FROM node@sha256:18aacc7993a16f1d766c21e3bff922e830bcdc7b549bbb789ceb7374a6138480 as npm
LABEL maintainer="esthesis@eurodyn.com"

# Set working directory
WORKDIR /app

# Install and cache app dependencies
COPY package.json /app/package.json
RUN npm install

#####################################################################################################
## Build application.
#####################################################################################################
# node:lts-bookworm-slim
FROM node@sha256:18aacc7993a16f1d766c21e3bff922e830bcdc7b549bbb789ceb7374a6138480 as build
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
# nginx:stable
FROM nginx@sha256:9007fdd404f9b8128362e8dbe1e358690490f8432e01ce124e1c815071d26af3
LABEL maintainer="esthesis@eurodyn.com"

# Copy artifact build from the 'build environment'.
COPY --from=build /app/dist /usr/share/nginx/html

# Patch nginx configuration to amend try_files.
RUN sed -i 's/location \/ {/location \/ {\n        try_files \$uri \$uri\/ \/index\.html \=404;\n/g' /etc/nginx/conf.d/default.conf

# Expose port 80
EXPOSE 80
