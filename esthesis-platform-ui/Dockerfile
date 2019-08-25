####################################################################################################
# Build application.
####################################################################################################
FROM node:12.2.0 as build
MAINTAINER esthesis@eurodyn.com

# set working directory
WORKDIR /app

# add `/app/node_modules/.bin` to $PATH
ENV PATH /app/node_modules/.bin:$PATH

# install and cache app dependencies
COPY package.json /app/package.json
RUN npm install
RUN npm install -g @angular/cli@7.3.0

# add app
COPY . /app

# generate build
RUN ng build --prod --output-path=dist

####################################################################################################
# Create runtime container.
####################################################################################################
FROM nginx:1.16.0-alpine
MAINTAINER esthesis@eurodyn.com

# Copy artifact build from the 'build environment'.
COPY --from=build /app/dist /usr/share/nginx/html

# Copy nginx configuration.
COPY docker/conf.nginx /etc/nginx/conf.d/default.conf

# Expose port 80
EXPOSE 80

# Run nginx
CMD ["nginx", "-g", "daemon off;"]
