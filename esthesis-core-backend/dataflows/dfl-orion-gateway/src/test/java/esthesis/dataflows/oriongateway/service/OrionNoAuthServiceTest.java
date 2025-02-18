package esthesis.dataflows.oriongateway.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class OrionNoAuthServiceTest {


	OrionNoAuthService orionNoAuthService = new OrionNoAuthService();

	@Test
	void authenticate() {
		assertDoesNotThrow(() -> orionNoAuthService.authenticate(null));
	}
}
