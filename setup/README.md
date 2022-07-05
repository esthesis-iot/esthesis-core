# Installation

## Mandatory components

### APISIX & APISIX Ingress Controller

```
helm repo add apisix https://charts.apiseven.com
helm upgrade --install apisix apisix/apisix --version 0.10 \
  --set ingress-controller.enabled=true \
  --set etcd.replicaCount=1 \
  --set admin.cors=false \
  --set gateway.type=LoadBalancer \
  --set "admin.allow.ipList[0]"="0.0.0.0/0" \
  --set admin.credentials.admin=esthesis-admin-key \
  --set ingress-controller.config.apisix.serviceName=apisix-admin \
  --set ingress-controller.config.apisix.serviceNamespace=esthesis-dev \
  --set ingress-controller.config.apisix.adminKey=esthesis-admin-key
```

### Kafka

```
helm repo add bitnami https://charts.bitnami.com/bitnami
helm  upgrade --install kafka bitnami/kafka --version 18.0.0 
```

To access from your dev machine, port-forward the Kafka service and add:
--set advertisedListeners\[0\]=CLIENT://localhost:9092 \
--set advertisedListeners\[1\]=INTERNAL://kafka-0.kafka-headless.esthesis-dev.svc.cluster.local:9093

## Equivalent components

### Keycloak

https://artifacthub.io/packages/helm/codecentric/keycloak/18.1.1?modal=install
Here is how you can install Keycloak with a self-signed certificate, using APISIX ingress
controller:

```
helm repo add bitnami https://charts.bitnami.com/bitnami
helm upgrade --install keycloak bitnami/keycloak --version 9.2.9 \
  --set auth.adminUser=esthesis \
  --set auth.adminPassword=esthesis \
  --set postgresql.auth.password=esthesis \
  --set service.type=LoadBalancer \
  --set proxy=none
```

### Creating a Keycloak realm for esthesis

- Click on 'Master > Add Realm'.
- Name the realm 'esthesis'.
- Click on 'Clients > Create'.
- Name the client 'esthesis'.
- Set 'Access Type' to 'confidential'.
- Set Valid Redirect URIs to '*'.
- Click on 'Users > Add user'.
- Set 'Username' to 'esthesis-admin'.
- Click on 'Credentials' and set the 'Password' to 'esthesis-admin'.

### MongoDB

```
helm repo add bitnami https://charts.bitnami.com/bitnami
helm upgrade --install mongodb bitnami/mongodb --version 12.1.19 \
  --set auth.rootUser=root \
  --set auth.rootPassword=root \
  --set auth.usernames\[0\]=esthesis \
  --set auth.passwords\[0\]=esthesis \
  --set auth.databases\[0\]=esthesis \
  --set architecture=replicaset \
  --set auth.replicaSetKey=esthesis \
  --set replicaSetName=esthesis-rs
```

Liquibase needs `collMod` permission, so `esthesis` user should be made `dbAdmin`:

```
mongosh -u root -p root mongodb://localhost:27017/esthesis?authSource=admin --eval "db.grantRolesToUser('esthesis', ['dbAdmin'])"
```

Keep an eye on this:
https://github.com/quarkusio/quarkus/issues/25850

## Optional components

### APISIX Dashboard

```
helm repo add apisix https://charts.apiseven.com
helm upgrade --install apisix-dashboard apisix/apisix-dashboard --version 0.6.0 \
  --set "config.authentication.users[0].username"=esthesis \
  --set "config.authentication.users[0].password"=esthesis \
  --set apisix.enabled=false \
  --set etcd.enabled=false \
  --set etcd.enabled=false \
  --set etcd.host="{http://apisix-etcd:2379}"
```

## esthesis

helm upgrade --install esthesis . \
--set
global.oidc.discoveryEndpoint=http://192.168.21.2/realms/esthesis/.well-known/openid-configuration \
--set
global.oidc.introspectionEndpoint=http://192.168.21.2/realms/esthesis/protocol/openid-connect/token/introspect \
--set global.oidc.clientSecret=N6EKsuut0CHzRYDA1NUVF7cBeVBNv3kj
