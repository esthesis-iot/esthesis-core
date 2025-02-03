package esthesis.service.publicaccess.dto;

import lombok.Data;

/**
 * A representation of the OIDC configuration.
 */
@Data
public class OidcConfigDTO {

	private String authority;
	private String redirectUrl;
	private String postLogoutRedirectUri;
	private String clientId;
	private String scope;
	private String responseType;
	private boolean silentRenew;
	private boolean useRefreshToken;
	private int renewTimeBeforeTokenExpiresInSeconds;
	private int maxIdTokenIatOffsetAllowedInSeconds;
	private boolean ignoreNonceAfterRefresh;
	private String[] secureRoutes;
}
