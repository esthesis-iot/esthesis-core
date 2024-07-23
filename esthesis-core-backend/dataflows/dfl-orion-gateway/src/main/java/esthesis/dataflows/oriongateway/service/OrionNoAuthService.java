package esthesis.dataflows.oriongateway.service;

import jakarta.ws.rs.client.ClientRequestContext;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public class OrionNoAuthService implements  OrionAuthService{

	@Override
	public void authenticate(ClientRequestContext clientRequestContext) {
		log.debug("No authentication defined for orion requests.");
	}
}
