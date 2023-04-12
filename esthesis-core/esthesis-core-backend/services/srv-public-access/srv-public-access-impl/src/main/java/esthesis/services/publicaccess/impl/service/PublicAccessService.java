package esthesis.services.publicaccess.impl.service;

import esthesis.service.publicaccess.dto.OidcConfigDTO;
import javax.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Slf4j
@ApplicationScoped
public class PublicAccessService {

	@ConfigProperty(name = "esthesis.oidc.authority")
	String authority;

	@ConfigProperty(name = "esthesis.oidc.redirect-url")
	String redirectUrl;

	@ConfigProperty(name = "esthesis.oidc.post-logout-redirect-uri")
	String postLogoutRedirectUri;

	@ConfigProperty(name = "esthesis.oidc.client-id")
	String clientId;

	@ConfigProperty(name = "esthesis.oidc.scope")
	String scope;

	@ConfigProperty(name = "esthesis.oidc.response-type")
	String responseType;

	@ConfigProperty(name = "esthesis.oidc.silent-renew")
	boolean silentRenew;

	@ConfigProperty(name = "esthesis.oidc.use-refresh-token")
	boolean useRefreshToken;

	@ConfigProperty(name = "esthesis.oidc.renew-time-before-token-expires-in-seconds")
	int renewTimeBeforeTokenExpiresInSeconds;

	@ConfigProperty(name = "esthesis.oidc.max-id-token-iat-offset-allowed-in-seconds")
	int maxIdTokenIatOffsetAllowedInSeconds;

	@ConfigProperty(name = "esthesis.oidc.ignore-nonce-after-refresh")
	boolean ignoreNonceAfterRefresh;

	@ConfigProperty(name = "esthesis.oidc.secure-routes")
	String[] secureRoutes;

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
