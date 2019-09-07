####################################################################################################
# Create runtime container.
####################################################################################################
FROM nginx:1.16.0-alpine
MAINTAINER esthesis@eurodyn.com

# Copy nginx configuration.
COPY docker/conf.nginx /etc/nginx/conf.d/default.conf

# Expose port 80
EXPOSE 80

# Run nginx
CMD ["nginx", "-g", "daemon off;"]
