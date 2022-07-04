package esthesis.service.device.dto;

import esthesis.common.dto.GenericResponse;
import esthesis.common.dto.MQTTServer;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@RegisterForReflection
@EqualsAndHashCode
@Accessors(chain = true)
public class RegistrationResponse implements GenericResponse {

  // The public key created for the device, encoded in Base64.
  private String publicKey;

  // The private key created for the device, encoded in Base64.
  private String privateKey;

  // The public key of the platform, encoded in Base64.
  private String psPublicKey;

  // A session key to encrypt content, encoded in Base64. The first 16 bytes of the session key
  // comprise of the IV.
  private String sessionKey;

  // The MQTT server to use to submit data.
  private MQTTServer mqttServer;

  // The URL to retrieve provisioning packages from.
  private String provisioningUrl;

  // The key with which provisioning packages are encrypted with.
  private String provisioningKey;

  // The certificate of the root CA.
  private String rootCaCertificate;

  // The certificate created for this device.Z
  private String certificate;
}
