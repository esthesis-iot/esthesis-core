package esthesis.platform.server.dto.jwt;

import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Encapsulates a reply from a verification request. It contains an indicator of whether the JWT
 * was found to be valid, together with an error description in case the token was invalid.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JWTClaimsResponseDTO {
  // Indicates whether this JWT successfully passed verification or not.
  private boolean valid;

  // The error message resulted during an unsuccessful verification.
  private String errorMessage;

  // The claims found on the JWT. This map contains all standard claims as well as custom claims
  // placed into the original JWT. Standard claims are named after their RFC-defined names
  // (https://tools.ietf.org/html/rfc7519), however this class also provide helper getters to
  // access them (e.g. getIssuer(), getIssuedAt(), etc.
  private Map<String, Object> claims;

  public String getSubject() {
    return claims != null ? (String)claims.get(Claims.SUBJECT) : null;
  }
}
