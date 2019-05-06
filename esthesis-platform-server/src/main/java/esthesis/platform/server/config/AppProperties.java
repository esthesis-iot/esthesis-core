package esthesis.platform.server.config;

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
@ConfigurationProperties(prefix = "esthesis.platform")
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

  private String nodeId;
}
