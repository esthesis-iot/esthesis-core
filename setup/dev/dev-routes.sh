#!/usr/bin/env bash

export APISIX_LOCAL_PORT=10000
export DEV_IP=192.168.100.100
#export CLIENT_SECRET=hlrVMWbgXotmIlBQvCu0V18GHrPgT7WM
export KEYCLOAK_IP=192.168.21.2

####################################################################################################
# PROTECTED RESOURCES
####################################################################################################
serviceNames=( auth device tag )
servicePorts=( 59000 59010 59020 )

for i in "${!serviceNames[@]}"
do
#  echo ${serviceNames[$i]}
#  echo ${servicePorts[$i]}
  echo "****** REGISTERING DEV ROUTE FOR SERVICE/PORT: ${serviceNames[$i]} / ${servicePorts[$i]}"
  curl -s http://127.0.0.1:$APISIX_LOCAL_PORT/apisix/admin/routes/dev-srv-${serviceNames[$i]}-route -H 'X-API-KEY: esthesis-admin-key' -X PUT -i -d '
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
          },
          "cors": {
            "origin": "*"
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
  curl -s http://127.0.0.1:$APISIX_LOCAL_PORT/apisix/admin/routes/dev-srv-${serviceNames[$i]}-route -H 'X-API-KEY: esthesis-admin-key' -X PUT -i -d '
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
