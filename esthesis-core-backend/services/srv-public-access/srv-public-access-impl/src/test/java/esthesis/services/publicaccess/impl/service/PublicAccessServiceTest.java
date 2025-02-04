package esthesis.services.publicaccess.impl.service;

import esthesis.service.publicaccess.dto.OidcConfigDTO;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class PublicAccessServiceTest {

	@Inject
	PublicAccessService publicAccessService;

	@Test
	void getOidcConfig() {
		// Assert OIDC configuration can be retrieved using the test values.
		OidcConfigDTO oidcConfig =  publicAccessService.getOidcConfig();
		assertEquals("test-oidc-client-id", oidcConfig.getClientId());
		assertEquals("test-oidc-scope", oidcConfig.getScope());
		assertEquals("test-oidc-response-type", oidcConfig.getResponseType());
		assertTrue(oidcConfig.isSilentRenew());
		assertTrue(oidcConfig.isUseRefreshToken());
		assertEquals(60, oidcConfig.getRenewTimeBeforeTokenExpiresInSeconds());
		assertEquals(20, oidcConfig.getMaxIdTokenIatOffsetAllowedInSeconds());
		assertTrue(oidcConfig.isIgnoreNonceAfterRefresh());
		assertEquals("test-oidc-secure-routes", oidcConfig.getSecureRoutes()[0]);
		assertEquals("test-oidc-authority", oidcConfig.getAuthority());
		assertEquals("test-oidc-redirect-url", oidcConfig.getRedirectUrl());
		assertEquals("test-oidc-post-logout-redirect-uri", oidcConfig.getPostLogoutRedirectUri());
	}
}
