package esthesis.service.device.dto.agent;

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
public class RegistrationResponse {

  // The certificate created for this device.
  private String certificate;

  // The public key created for the device, encoded in Base64.
  private String publicKey;

  // The private key created for the device, encoded in Base64.
  private String privateKey;

  // The URL of the MQTT server to use to submit data.
  private String mqttServer;

  // The URL to retrieve provisioning packages from.
  private String provisioningUrl;

  // The certificate of the root CA.
  private String rootCaCertificate;

}
