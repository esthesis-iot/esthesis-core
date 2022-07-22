#!/usr/bin/env bash

SUGGESTED_APISIX_KEY=esthesis-admin-key
if [ ! "$1" = "--auto" ]; then
  echo -e "Enter your APISIX key ($SUGGESTED_APISIX_KEY): \c"
  read APISIX_KEY
fi
[ -z "$APISIX_KEY" ] && APISIX_KEY=$SUGGESTED_APISIX_KEY
echo "Setting APISIX KEY to: $APISIX_KEY"

SUGGESTED_DEV_IP=$(ifconfig |grep 192.168 | awk '{print $2}')
if [ ! "$1" = "--auto" ]; then
  echo -e "Enter your development machine IP address ($SUGGESTED_DEV_IP): \c"
  read DEV_IP
fi
[ -z "$DEV_IP" ] && DEV_IP=$SUGGESTED_DEV_IP
echo "Setting development machine IP to: $DEV_IP"

# APISIX admin listens on 9180, however if you're using Logitech Hub software port 9180 is used.
SUGGESTED_APISIX_PORT=19180
if [ ! "$1" = "--auto" ]; then
  echo -e "Enter your localhost APISIX admin port ($SUGGESTED_APISIX_PORT): \c"
  read APISIX_PORT
fi
[ -z "$APISIX_PORT" ] && APISIX_PORT=$SUGGESTED_APISIX_PORT
echo "Setting localhost APISIX admin port to: $APISIX_PORT"

SUGGESTED_KEYCLOAK_IP=$(kubectl get svc/keycloak -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
if [ ! "$1" = "--auto" ]; then
  echo -e "Enter your Keycloak localhost IP address ($SUGGESTED_KEYCLOAK_IP): \c"
  read KEYCLOACK_IP
fi
[ -z "$KEYCLOACK_IP" ] && KEYCLOACK_IP=$SUGGESTED_KEYCLOAK_IP
echo "Setting Keycloak IP to: $KEYCLOACK_IP"

####################################################################################################
# PROTECTED RESOURCES
####################################################################################################
serviceNames=( auth device tag crypto registry )
servicePorts=( 59000 59010 59020 59040 59030 )

for i in "${!serviceNames[@]}"
do
  echo "****** REGISTERING DEV ROUTE FOR SERVICE/PORT: ${serviceNames[$i]} / ${servicePorts[$i]}"
  curl -s http://127.0.0.1:$APISIX_PORT/apisix/admin/routes/dev-srv-${serviceNames[$i]}-route -H "X-API-KEY: $APISIX_KEY" -X PUT -i -d '
  {
      "name": "dev-srv-'${serviceNames[$i]}'-route",
      "uris": ["/dev/api/v1/'${serviceNames[$i]}'", "/dev/api/v1/'${serviceNames[$i]}'/*"],
      "upstream": {
          "type": "roundrobin",
          "nodes": {
              "'${DEV_IP}':'${servicePorts[$i]}'": 1
          }
      },
      "plugins": {
          "opentelemetry": {
            "sampler": {
              "name": "always_on"
            }
          },
          "proxy-rewrite": {
              "regex_uri": [
                "/dev/(.*)",
                "/$1"
              ]
          },
          "cors": {
              "origin": "*"
          },
          "openid-connect": {
            "client_id": "esthesis",
            "client_secret": "",
            "discovery": "http://'$KEYCLOAK_IP'/realms/esthesis/.well-known/openid-configuration",
            "scope": "openid profile",
            "use_jwks": true,
            "bearer_only": true,
            "realm": "esthesis",
            "redirect_uri": "/dev/api/v1/'${serviceNames[$i]}'/callback",
            "set_access_token_header": false,
            "set_id_token_header": false,
            "set_userinfo_header": false
          }
      }
  }'
  echo
done

####################################################################################################
# PUBLIC RESOURCES
####################################################################################################
serviceNames=( conf-public )
servicePorts=( 58000 )

for i in "${!serviceNames[@]}"
do
#  echo ${serviceNames[$i]}
#  echo ${servicePorts[$i]}
  echo "****** REGISTERING DEV ROUTE FOR SERVICE/PORT: ${serviceNames[$i]} / ${servicePorts[$i]}"
  curl -s http://127.0.0.1:$APISIX_PORT/apisix/admin/routes/dev-srv-${serviceNames[$i]}-route -H "X-API-KEY: $APISIX_KEY" -X PUT -i -d '
  {
      "name": "dev-srv-'${serviceNames[$i]}'-route",
      "uris": ["/dev/api/v1/'${serviceNames[$i]}'", "/dev/api/v1/'${serviceNames[$i]}'/*"],
      "upstream": {
          "type": "roundrobin",
          "nodes": {
              "'${DEV_IP}':'${servicePorts[$i]}'": 1
          }
      },
      "cors": {
        "origin": "*"
      },
      "plugins": {
          "proxy-rewrite": {
              "regex_uri": [
                "/dev/(.*)",
                "/$1"
              ]
          }
      }
  }'
  echo
done
