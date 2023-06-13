# MQTT security hardening

The default Helm scripts for esthesis dependencies deploy an MQTT broker with no security
configured. This may be convenient to make sure everything works in your environment, however by no
means this is a production-ready setup. As esthesis uses the topic names to connect device IDs with
the actual devices managed in the system, it is of paramount importance to enable security in MQTT
before you expose your installation outside a controlled network.

The following instructions show you how you can enable certificate-based authentication using
Eclipse Mosquitto, which is the MQTT broker being used when you set up esthesis using the provider
Helm charts.

## Create a Certificate Authority and a Certificate
Create a Certificate Authority that will be signing all certificates to be used
by esthesis Core itself, including the certificates that are pushed to devices during registration.

### Create a Certificate Authority
To create a Certificate Authority, go to Key Management > CAs and click on the "Create" button.
If you have already configured a CA before, you can skip this part.

### Create a Certificate
You need to create a certificate to be used by the MQTT server to establish TLS. To create a
Certificate, go to Key Management > Certificates and click on the "Create" button. Pay attention to
the following points:
1. The certificate should be signed by the CA you created above.
2. The CN of the certificate should match the domain where the MQTT server is accessible from your
devices' perspective. You can add additional domains as SANs (for example, the domain name of the
service under which the MQTT server is accessible from within the cluster, i.e. mosquitto).

## Download the CA and the Certificate
Download the private key and the certificate for the certificate you created above and the
certificate for the CA.

## Assign the CA and the certificate as defaults
Navigate to Settings > Security and assign the CA and the certificate as defaults.

## Create an ACL file
Create a text file using the following command:
```shell
cat > aclfile.conf << EOF
pattern write $SYS/broker/connection/%c/state
pattern write esthesis/ping/%u
pattern write esthesis/telemetry/%u
pattern write esthesis/metadata/%u
pattern read esthesis/control/request/%u
pattern write esthesis/control/reply/%u
user esthesis-platform
topic esthesis/#
EOF
```
:::caution
1. The username you specify in `user` parameter, should match the common name of the certificate
you created above.
2. If you have used different topic names than the default ones, change the topic names accordingly.
:::

## Create a Mosquitto configuration file
Create a text file using the following command:
```shell
cat > mosquitto.conf << EOF
port 8883
cafile /mosquitto/config/ca.crt
certfile /mosquitto/config/cert.crt
keyfile /mosquitto/config/cert.key
allow_anonymous false
require_certificate true
use_identity_as_username true
acl_file /mosquitto/config/aclfile.conf
EOF
```

## Create a Kubernetes secret
Create a Kubernetes secret to store the files you downloaded above:
```shell
kubectl create secret generic esthesis-mqtt-secret \
    --from-file=ca.crt=ca.crt \
    --from-file=cert.crt=cert.crt \
    --from-file=cert.key=cert.key \
    --from-file=aclfile.conf=aclfile.conf \
    --from-file=mosquitto.conf=mosquitto.conf
```
:::caution
Change the `.crt` and `.key` filenames to match those you downloaded above.
:::

## Prepare a patch for the Mosquitto deployment
Create a patch file using the following command:
```shell
cat > patch-mosquitto.yaml << EOF
spec:
  template:
    spec:
      containers:
        - name: mosquitto
          volumeMounts:
            - name: esthesis-mqtt-secret
              mountPath: "/mosquitto/config"
          startupProbe:
            tcpSocket:
              port: 8883
          livenessProbe:
            tcpSocket:
              port: 8883
      volumes:
        - name: esthesis-mqtt-secret
          secret:
            secretName: esthesis-mqtt-secret
            items:
              - key: ca.crt
                path: ca.crt
              - key: cert.crt
                path: cert.crt
              - key: cert.key
                path: cert.key
              - key: mosquitto.conf
                path: mosquitto.conf
              - key: aclfile.conf
                path: aclfile.conf
EOF
```

## Patch the Mosquitto deployment
Apply the patch using the following command:
```shell
kubectl patch deployment mosquitto --patch "$(cat patch-mosquitto.yaml)"
```

## Warning - Losing connectivity
:::warning
Before you apply the above procedure, make sure devices already provisioned to esthesis Core have
been issued with a certificate signed by the CA used above. Otherwise, they will lose connectivity
to the MQTT broker.

In addition, since the MQTT broker will no longer accept non-TLS connections, you need to update
the URL of the MQTT broker used by the devices to start with `ssl://` instead of `tcp://`.
:::
