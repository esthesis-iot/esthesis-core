package esthesis.platform.server.dto.jwt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A placeholder for an encoded JWT.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JWTTokenDTO {
  private String jwt;
}
