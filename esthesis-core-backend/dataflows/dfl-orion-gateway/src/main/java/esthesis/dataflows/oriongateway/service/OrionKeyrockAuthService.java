package esthesis.dataflows.oriongateway.service;

import esthesis.dataflows.oriongateway.client.OrionKeyrockAuthClient;
import esthesis.dataflows.oriongateway.config.AppConfig;
import esthesis.dataflows.oriongateway.dto.OrionKeyrockAccessTokenDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.ClientRequestContext;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * This class is responsible for authenticating the requests to the Orion Context Broker. It uses
 * the Keyrock Identity Manager to authenticate the requests. The access token is cached in memory
 * and renewed when it expires.
 */
@Slf4j
@ApplicationScoped
public class OrionKeyrockAuthService implements OrionAuthService {

	// Injected on constructor.
	OrionKeyrockAuthClient keyrockAuthClient;

	// Injected on constructor.
	AppConfig appConfig;

	// Access Token cached in memory.
	OrionKeyrockAccessTokenDTO cachedAccessToken;

	// Configurations values set on constructor.
	String username;
	String password;
	String grantType;
	String basicToken;


	/**
	 * Constructor for the OrionKeyrockAuthService.
	 * @param appConfig The application configuration.
	 * @param keyrockAuthClient The Keyrock authentication client.
	 */
	@Inject
	public OrionKeyrockAuthService(AppConfig appConfig, @RestClient OrionKeyrockAuthClient keyrockAuthClient) {
		this.appConfig = appConfig;
		this.username = appConfig.orionAuthenticationUsername().orElse("");
		this.password = appConfig.orionAuthenticationPassword().orElse("");
		this.grantType = appConfig.orionAuthenticationGrantType().orElse("");
		this.basicToken = "Basic " + appConfig.orionAuthenticationCredentialToken().orElse("");
		this.keyrockAuthClient = keyrockAuthClient;
	}

	/**
	 * Authenticates the request by adding the Authorization header with the access token.
	 *
	 * @param clientRequestContext The request context.
	 */
	@Override
	public void authenticate(ClientRequestContext clientRequestContext) {
		clientRequestContext.getHeaders().add("Authorization", "Bearer " + getValidToken());
	}

	/**
	 * Gets the valid access token. If the token is not valid or expired, it renews it.
	 *
	 * @return The valid access token.
	 */
	private String getValidToken() {
		if (Objects.isNull(cachedAccessToken) || Instant.now()
			.isAfter(cachedAccessToken.getExpirationTime())) {
			renewToken();
		}
		return cachedAccessToken.getAccessToken();
	}

	/**
	 * Renews the access token.
	 */
	private void renewToken() {
		log.debug("Renewing Keyrock access token...");
		cachedAccessToken = keyrockAuthClient.requestAccessToken(username, password, grantType,
			basicToken);
		// Subtracts 10 seconds from the expiration time (3600) just in case.
		int expirationSecondsAdjusted = cachedAccessToken.getExpiresIn() - 10;
		cachedAccessToken.setExpirationTime(
			Instant.now().plus(expirationSecondsAdjusted, ChronoUnit.SECONDS));

		log.debug("Keyrock access token set to '{}' with expiration in '{}'.",
			cachedAccessToken.getAccessToken(),
			cachedAccessToken.getExpirationTime());
	}
}
