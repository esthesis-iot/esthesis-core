# esthesis Device runtime
The device runtime is the piece of software running in your device connecting
it to the esthesis platform.

To build jar:

```bash
mvn clean install spring-boot:repackage -f pom.xml
```

Example:

```bash
hardwareId=device:Rpi3bp \
storageRoot="/c/Users/Nisargam/.esthesis/devices/device1" \
secureStorageRoot="/c/Users/Nisargam/.esthesis/devices/device1/secure" \
registrationUrl="http://192.168.2.20:46000" \
incomingSigned=false \
incomingEncrypted=false \
provisioningSigned=false \
provisioningEncrypted=false \
outgoingSigned=false \
outgoingEncrypted=false \
tags=greece \
proxyMqtt=true \
proxyWeb=true \
java -jar esthesis-device-java-1.0.0-SNAPSHOT.jar
```
