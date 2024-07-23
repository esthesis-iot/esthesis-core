package esthesis.dataflows.oriongateway.service;

import jakarta.ws.rs.client.ClientRequestContext;

/**
 * Interface for orion authentication services to use
 */
public interface OrionAuthService {

	// Method to be called in each request to orion
	void authenticate(ClientRequestContext clientRequestContext);
}
