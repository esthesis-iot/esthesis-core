package esthesis.platform.backend.server.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

/**
 * The list of properties defined in application.properties.
 */
@Data
@Configuration
@NoArgsConstructor
@ConfigurationProperties(prefix = "esthesis")
public class AppProperties {
  private final String randomNodeId = UUID.randomUUID().toString();

  private int securitySymmetricKeySize;
  private String securitySymmetricKeyAlgorithm;
  private String securitySymmetricCipherAlgorithm;

  private int securityAsymmetricKeySize;
  private String securityAsymmetricKeyAlgorithm;
  private String securityAsymmetricCipherAlgorithm;
  private String securityAsymmetricSignatureAlgorithm;

  // The secret to sign JWT.
  private String jwtSecret;

  // The number of minutes a JWT is valid for.
  private int jwtTtlMinutes;

  // The issuer of the JWT.
  private String jwtIssuer;

  // The ID of this node. If missing, a random one will be created each time this node boots.
  private String nodeId;

  // The root of the filesystem where the platform can store data (for example,
  // provisioning packages). If left empty, a folder under `.esthesis` on the user's home dir will
  // be created. Note that in cluster configurations this filesystem needs to be shared among the
  // instances of the cluster.
  private String fsRoot;

  // The location in which provisioning packages are stored. This is set automatically by
  // `FSContentConfig` and it is always under `${fsRoot}/provisioning`.
  private String fsProvisioningRoot;

  // The location of a temporary folder. This is set automatically by `FSContentConfig` and it is
  // always under `${fsRoot}/.tmp`. The temporary folder is automatically cleaned up when a node
  // is restarted (the node clears its own temporary files) and also hourly (by removing
  // temporary files older than one hour).
  private String fsTmpRoot;

  // The HTTP port NiFi listens for Data Producers requests.
  private int nifiProducersHttpPort = 20000;
}

