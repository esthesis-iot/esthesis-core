package esthesis.dataflows.oriongateway.service;

import esthesis.dataflows.oriongateway.client.OrionKeyrockAuthClient;
import esthesis.dataflows.oriongateway.config.AppConfig;
import esthesis.dataflows.oriongateway.dto.OrionKeyrockAccessTokenDTO;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrionKeyrockAuthServiceTest {

	@Mock
	OrionKeyrockAuthClient keyrockAuthClient;

	@Mock
	AppConfig appConfig;

	@Mock
	ClientRequestContext clientRequestContext;

	OrionKeyrockAuthService orionKeyrockAuthService;

	MultivaluedMap<String, Object> headers;


	@BeforeEach
	void setUp() {
		// Initialize mocks.
		MockitoAnnotations.openMocks(this);

		// Mock credentials configuration.
		when(appConfig.orionAuthenticationUsername()).thenReturn(Optional.of("username"));
		when(appConfig.orionAuthenticationPassword()).thenReturn(Optional.of("password"));
		when(appConfig.orionAuthenticationGrantType()).thenReturn(Optional.of("grantType"));
		when(appConfig.orionAuthenticationCredentialToken()).thenReturn(Optional.of("credentialToken"));
		when(appConfig.orionAuthenticationUrl()).thenReturn(Optional.of("http://localhost:8080"));

		// Mock retrieving the access token.
		when(keyrockAuthClient.requestAccessToken(anyString(), anyString(), anyString(), anyString()))
			.thenReturn(
				new OrionKeyrockAccessTokenDTO(
					"accessToken",
					3600,
					"refreshToken",
					"tokenType",
					List.of("scope"),
					Instant.now().plus(3600, ChronoUnit.SECONDS)));


		// Mock headers.
		headers = new MultivaluedHashMap<>();
		when(clientRequestContext.getHeaders()).thenReturn(headers);

		// Create the service.
		orionKeyrockAuthService = new OrionKeyrockAuthService(appConfig, keyrockAuthClient);

	}

	@Test
	void authenticate() {
		// Perform the authentication.
		orionKeyrockAuthService.authenticate(clientRequestContext);

		// Verify the request to retrieve the access token was made.
		verify(keyrockAuthClient, Mockito.times(1))
			.requestAccessToken("username", "password", "grantType", "Basic credentialToken");

		// Verify the Authorization header was added to the request.
		assertEquals("Bearer accessToken", clientRequestContext.getHeaders().getFirst("Authorization"));

		// Perform the authentication again.
		orionKeyrockAuthService.authenticate(clientRequestContext);

		// Verify the request to retrieve the access token has not increased which means it has not being called again.
		verify(keyrockAuthClient, Mockito.times(1))
			.requestAccessToken("username", "password", "grantType", "Basic credentialToken");

		// Verify the Authorization header is sill present in the request.
		assertEquals("Bearer accessToken", clientRequestContext.getHeaders().getFirst("Authorization"));
	}
}
