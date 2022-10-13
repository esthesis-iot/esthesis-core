####################################################################################################
# Docker multistage build for esthesis server UI.
# Build this Dockerfile from `esthesis-platform/` by executing `docker compose build esthesis-ui`.
####################################################################################################

####################################################################################################
# Cache NPM modules.
####################################################################################################
FROM node:16.14.2-alpine as npm
LABEL maintainer="esthesis@eurodyn.com"

# Set working directory
WORKDIR /app

# Add `/app/node_modules/.bin` to $PATH
ENV PATH /app/node_modules/.bin:$PATH

# Install and cache app dependencies
COPY package.json /app/package.json
RUN npm install --force

####################################################################################################
# Build application.
####################################################################################################
FROM node:16.14.2-alpine as build
LABEL maintainer="esthesis@eurodyn.com"

# Set working directory
WORKDIR /app

# Add `/app/node_modules/.bin` to $PATH
ENV PATH /app/node_modules/.bin:$PATH

# Install and cache app dependencies
COPY --from=npm /app/node_modules /app/node_modules
COPY package.json /app/package.json
RUN npm install -g @angular/cli

# Add app
COPY . /app

# Generate build
RUN ng build --configuration production --output-path=dist

####################################################################################################
# Create runtime container.
####################################################################################################
FROM nginx:1.19.7-alpine
LABEL maintainer="esthesis@eurodyn.com"

# Copy artifact build from the 'build environment'.
COPY --from=build /app/dist /usr/share/nginx/html

# Copy nginx configuration.
COPY docker/conf.nginx /etc/nginx/templates/default.conf.template

# Expose port 80
EXPOSE 8080

# Run nginx
CMD ["nginx", "-g", "daemon off;"]
