package esthesis.service.agent.impl.resource;

import esthesis.common.agent.dto.AgentRegistrationRequest;
import esthesis.common.agent.dto.AgentRegistrationResponse;
import esthesis.common.exception.QLimitException;
import esthesis.common.exception.QSecurityException;
import esthesis.service.agent.impl.service.AgentService;
import esthesis.service.agent.resource.AgentResource;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.operator.OperatorCreationException;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;

@Slf4j
public class AgentResourceImpl implements AgentResource {

	@Inject
	AgentService agentService;

	public AgentRegistrationResponse register(AgentRegistrationRequest agentRegistrationRequest)
	throws NoSuchAlgorithmException, IOException, InvalidKeySpecException,
				 OperatorCreationException, NoSuchProviderException {
		log.debug("Received agent registration request '{}'.",
			agentRegistrationRequest);

		return agentService.register(agentRegistrationRequest);
	}

	@Override
	public Response findProvisioningPackage(String hardwareId, String version,
		Optional<String> token) {
		try {
			return Response.ok(agentService.findProvisioningPackage(hardwareId, version, token)).build();
		} catch (QLimitException e) {
			log.warn(e.getMessage());
			return Response.status(Status.TOO_MANY_REQUESTS).build();
		} catch (QSecurityException e) {
			log.warn(e.getMessage());
			return Response.status(Status.UNAUTHORIZED).build();
		}
	}

	@Override
	public Response findProvisioningPackageById(String hardwareId, String packageId,
		Optional<String> token) {
		log.debug("Received find provisioning package by id request for hardware id '{}' and package "
				+ "id '{}' with token '{}'.", hardwareId, packageId, token);
		try {
			return Response.ok(agentService.findProvisioningPackageById(hardwareId, packageId, token))
				.build();
		} catch (QLimitException e) {
			log.warn(e.getMessage());
			return Response.status(Status.TOO_MANY_REQUESTS).build();
		} catch (QSecurityException e) {
			log.warn(e.getMessage());
			return Response.status(Status.UNAUTHORIZED).build();
		}
	}

	@Override
	public Uni<RestResponse<byte[]>> downloadProvisioningPackage(String token) {
		Uni<byte[]> binary = agentService.downloadProvisioningPackage(token);
		return binary.onItem().transform(b -> ResponseBuilder.ok(b).build());
	}

}
