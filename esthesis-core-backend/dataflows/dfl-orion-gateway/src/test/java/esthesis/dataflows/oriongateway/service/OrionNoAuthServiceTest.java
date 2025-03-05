package esthesis.dataflows.oriongateway.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class OrionNoAuthServiceTest {


	OrionNoAuthService orionNoAuthService = new OrionNoAuthService();

	@Test
	void authenticate() {
		assertDoesNotThrow(() -> orionNoAuthService.authenticate(null));
	}
}
