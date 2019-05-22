package esthesis.platform.server.service;


import esthesis.extension.util.Base64E;
import esthesis.platform.server.config.AppConstants.Jwt;
import esthesis.platform.server.config.AppProperties;
import esthesis.platform.server.dto.jwt.JWTClaimsResponseDTO;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.collections4.MapUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class JWTService {

  private final AppProperties appProperties;

  // The number of seconds for clock skew when validating a JWT.
  private final long jwtSkewAllowed = 10;

  public JWTService(AppProperties appProperties) {
    this.appProperties = appProperties;
  }

  private boolean areCredentialsSet() {
    return (SecurityContextHolder.getContext().getAuthentication() != null
        && SecurityContextHolder.getContext().getAuthentication().getCredentials() != null);
  }

  public String getEmail() {
    if (areCredentialsSet()) {
      return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    } else {
      return null;
    }
  }

  public int getUserId() {
    if (areCredentialsSet()) {
      return (int) getClaimValue(String.valueOf(SecurityContextHolder.getContext().getAuthentication().getCredentials()),
          Jwt.JWT_CLAIM_USER_ID);
    } else {
      return 0;
    }
  }

  /**
   * Returns the user Id from a specific JWT.
   *
   * @param jwt The JWT to be used to extract the user Id information.
   * @return Returns the user Id found in the JWT or null.
   */
  public String getUserIdFromJwt(String jwt) {
    return (String) getClaimValue(jwt, Jwt.JWT_CLAIM_USER_ID);
  }

  public String generateJwt(String subject, long userId) {
    return generateJwt(subject, userId, null);
  }

  public String generateJwt(String subject, long userId, Map<String, Object> claims) {
    // The JWT signature algorithm to be used to sign the token.
    final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    // Set current time.
    final long nowMillis = System.currentTimeMillis();
    final Date now = new Date(nowMillis);

    // Sign JWT with the ApiKey secret
    final byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(
        Base64E.encode(appProperties.getJwtSecret().getBytes()));
    final Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

    // Prepare JWT properties..
    final JwtBuilder builder = Jwts.builder()
        .setId(UUID.randomUUID().toString())
        .setIssuedAt(now)
        .setSubject(subject)
        .setIssuer(appProperties.getJwtIssuer())
        .setExpiration(new Date(Instant.now().plus(appProperties.getJwtTtlMinutes(),
            ChronoUnit.MINUTES).toEpochMilli()))
        .signWith(signatureAlgorithm, signingKey);

    // Add additional claims if any.
    if (MapUtils.isNotEmpty(claims)) {
      builder.addClaims(claims);
    }

    // Add userId into claims.
    builder.claim(Jwt.JWT_CLAIM_USER_ID, userId);

    // Build the JWT and serialize it to a compact, URL-safe string.
    return builder.compact();
  }

  /**
   * Returns the claims found in a JWT while it is also verifying the token.
   */
  public JWTClaimsResponseDTO getClaims(String jwt) {
    final JWTClaimsResponseDTO jwtClaimsResponseDTO= new JWTClaimsResponseDTO();

    try {
      jwtClaimsResponseDTO.setClaims(
              Jwts.parser().setSigningKey(
                  Base64E.encode(appProperties.getJwtSecret().getBytes()))
                  .setAllowedClockSkewSeconds(jwtSkewAllowed)
                  .parseClaimsJws(jwt).getBody());
      jwtClaimsResponseDTO.setValid(true);

    } catch (Exception e) {
      jwtClaimsResponseDTO.setValid(false);
      jwtClaimsResponseDTO.setErrorMessage(e.getMessage());
    }

    return jwtClaimsResponseDTO;
  }

  /**
   * Returns the value of a specific claim in JWT while also verifying the JWT.
   *
   * @param claim The name of the claim to return.
   * @return The calue of the requested claim.
   */
  private Object getClaimValue(String jwt, String claim) {
    final JWTClaimsResponseDTO claims = getClaims(jwt);
    if (claims != null && claims.getClaims() != null && claims.getClaims().containsKey(claim)) {
      return claims.getClaims().get(claim);
    } else {
      return null;
    }
  }

  public Duration getJwtTTL() {
    return Duration.of(appProperties.getJwtTtlMinutes(), ChronoUnit.MINUTES);
  }
}
