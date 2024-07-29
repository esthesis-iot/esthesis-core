# MQTT security hardening

The default Helm charts for esthesis dependencies deploy an MQTT broker with no security
configured. This may be convenient to make sure everything works in your environment, however by no
means this is a production-ready setup. As esthesis uses the topic names to connect device IDs with
the actual devices managed in the system, it is of paramount importance to enable security in MQTT
before you expose your installation outside a controlled network.

The following instructions show you how you can enable certificate-based authentication using
Eclipse Mosquitto, which is the MQTT broker being used when you set up esthesis using the provider
Helm charts.

## Create a Certificate Authority and a Certificate
To enable certificate-based authentication and mutual TLS, you need to create a Certificate
Authority (CA) and a server certificate.

The CA will be responsible to sign the server certificate, and should be the same CA that signs
the certificates used by the devices to connect to the MQTT broker.

The server certificate will be used by the MQTT broker to establish a TLS connections with the devices.

### Create a Certificate Authority
To create a Certificate Authority, go to Key Management > CAs and click on the "Create" button.
If you have already configured a CA before, you can skip this part. Make sure the CA you create
here is the one set as the Root CA under Settings > Security.

### Create a server Certificate
You need to create a certificate to be used by the MQTT server to establish TLS. To create a
Certificate, go to Key Management > Certificates and click on the "Create" button. Pay attention to
the following points:
1. The certificate should be signed by the CA you created above.
2. The CN of the certificate should match the domain where the MQTT server is accessible from your
devices' perspective. You can add additional domains as SANs (for example, the domain name of the
service under which the MQTT server is accessible from within the cluster, i.e. mosquitto).

### Download the CA and the Server Certificate
Download the private key and the certificate for the server certificate you created above and the
certificate for the CA.

## Redeploy the Helm chart enabling TLS
Go to the location where you deployed the esthesis dependencies via executing `helmfile sync`
and add the following environment variables:

```shell
export MOSQUITTO_MUTUAL_TLS=true
export MOSQUITTO_CA_CERT=$(cat ca.crt | base64)
export MOSQUITTO_SERVER_CERT=$(cat cert.crt | base64)
export MOSQUITTO_SERVER_KEY=$(cat cert.key | base64)
```

:::info
1. Replace the names of ca.crt, cert.crt and cert.key with the files you downloaded above.
2. The name of the user specified by `MOSQUITTO_SUPER_USER` will be the "superuser" for the MQTT, i.e.
a user that can subscribe and publish to/from any topic. This user should be used by the esthesis
platform to communicate with the MQTT broker. Create a separate certificate for this user, having
the Common Name set to the same name as the one you specified by `MOSQUITTO_SUPER_USER` (if you
did not specify a value for `MOSQUITTO_SUPER_USER`, the default value is 'esthesis').
3. During the deployment of the supporting infrastructure you, probably, had to define an array of
environment variables to be used by the Helm charts. Do not forget to re-specify these variables
before you re-run the `helmfile sync` command here, otherwise the deployment might fail or have
unexpected results.
:::

## Warnings
:::warning Losing connectivity with your devices
Before you apply the above procedure, make sure devices already provisioned to esthesis CORE have
been issued with a certificate signed by the CA used above. Otherwise, they will lose connectivity
to the MQTT broker.

In addition, since the MQTT broker will no longer accept non-TLS connections, you need to update
the URL of the MQTT broker used by the devices to start with `ssl://` instead of `tcp://`.
:::

:::warning Dataflows failing to connect
If you have configured a dataflow that accesses the MQTT broker, you need to update it to point
to the TLS URL of the MQTT broker. In addition, you need to update the dataflow to use a certificate
to identify itself to the MQTT broker.

Be careful which certificate you will use. Dataflows accessing the MQTT broker, usually, require
full-access, that means to be able to subscribe and publish to/from any topic. If you have used the
provided Mosquitto configuration, you need to use the certificate of the "superuser" you created
while hardening the security of the MQTT broker.
:::
