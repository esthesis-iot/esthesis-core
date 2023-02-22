package esthesis.services.publicaccess.impl.service;

import esthesis.service.publicaccess.dto.OidcConfigDTO;
import esthesis.service.publicaccess.resource.PublicAccessResource;
import org.eclipse.microprofile.config.inject.ConfigProperty;

public class PublicAccessResourceImpl implements PublicAccessResource {

  @ConfigProperty(name = "esthesis.oidc.authority")
  private String authority;

  @ConfigProperty(name = "esthesis.oidc.redirect-url")
  private String redirectUrl;

  @ConfigProperty(name = "esthesis.oidc.post-logout-redirect-uri")
  private String postLogoutRedirectUri;

  @ConfigProperty(name = "esthesis.oidc.client-id")
  private String clientId;

  @ConfigProperty(name = "esthesis.oidc.scope")
  private String scope;

  @ConfigProperty(name = "esthesis.oidc.response-type")
  private String responseType;

  @ConfigProperty(name = "esthesis.oidc.silent-renew")
  private boolean silentRenew;

  @ConfigProperty(name = "esthesis.oidc.use-refresh-token")
  private boolean useRefreshToken;

  @ConfigProperty(name = "esthesis.oidc.renew-time-before-token-expires-in-seconds")
  private int renewTimeBeforeTokenExpiresInSeconds;

  @ConfigProperty(name = "esthesis.oidc.max-id-token-iat-offset-allowed-in-seconds")
  private int maxIdTokenIatOffsetAllowedInSeconds;

  @ConfigProperty(name = "esthesis.oidc.ignore-nonce-after-refresh")
  private boolean ignoreNonceAfterRefresh;

  @ConfigProperty(name = "esthesis.oidc.secure-routes")
  private String[] secureRoutes;

  @Override
  public OidcConfigDTO getOidcConfig() {
    OidcConfigDTO oidcConfigDTO = new OidcConfigDTO();
    oidcConfigDTO.setAuthority(authority);
    oidcConfigDTO.setRedirectUrl(redirectUrl);
    oidcConfigDTO.setPostLogoutRedirectUri(postLogoutRedirectUri);
    oidcConfigDTO.setClientId(clientId);
    oidcConfigDTO.setScope(scope);
    oidcConfigDTO.setResponseType(responseType);
    oidcConfigDTO.setSilentRenew(silentRenew);
    oidcConfigDTO.setUseRefreshToken(useRefreshToken);
    oidcConfigDTO.setRenewTimeBeforeTokenExpiresInSeconds(renewTimeBeforeTokenExpiresInSeconds);
    oidcConfigDTO.setMaxIdTokenIatOffsetAllowedInSeconds(maxIdTokenIatOffsetAllowedInSeconds);
    oidcConfigDTO.setIgnoreNonceAfterRefresh(ignoreNonceAfterRefresh);
    oidcConfigDTO.setSecureRoutes(secureRoutes);

    return oidcConfigDTO;
  }
}
