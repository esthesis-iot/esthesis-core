package esthesis.platform.server.dto.jwt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * A class encapsulating all data necessary to create a JWT.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JWTGenerateRequestDTO {
  // The secret to be used to sign the JWT. Use only Latin-1/ISO-8859-1 characters.
  private String secret;

  // The subject of this JWT. It can be anything as long as it makes sense to your application.
  private String subject;

  // The issuer of this JWT. It can be anything as long as it makes sense to your application.
  private String issuer;

  // The Id to isse the JWT with.
  private String id;

  // Claims to be included in the JWT.
  private Map<String, Object> claims;

  // The Time-To-Live (TTL) for the token in milliseconds. This is effectively setting the
  // expiration date for the JWT.
  private long ttl;
}
