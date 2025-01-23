package esthesis.dataflows.oriongateway.service;

import jakarta.ws.rs.client.ClientRequestContext;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * No authentication service for Orion requests.
 */
@Slf4j
@NoArgsConstructor
public class OrionNoAuthService implements OrionAuthService {

	/**
	 * No authentication defined for Orion requests.
	 *
	 * @param clientRequestContext The request context.
	 */
	@Override
	public void authenticate(ClientRequestContext clientRequestContext) {
		log.debug("No authentication defined for orion requests.");
	}
}
