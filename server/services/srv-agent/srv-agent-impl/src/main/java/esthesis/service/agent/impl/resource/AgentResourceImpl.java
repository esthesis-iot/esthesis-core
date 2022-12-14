package esthesis.service.agent.impl.resource;

import esthesis.common.exception.QLimitException;
import esthesis.common.exception.QSecurityException;
import esthesis.service.agent.dto.AgentRegistrationRequest;
import esthesis.service.agent.dto.AgentRegistrationResponse;
import esthesis.service.agent.impl.service.AgentService;
import esthesis.service.agent.resource.AgentResource;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.operator.OperatorCreationException;

@Slf4j
public class AgentResourceImpl implements AgentResource {

  @Inject
  AgentService agentService;

  public AgentRegistrationResponse register(
      AgentRegistrationRequest agentRegistrationRequest)
  throws NoSuchAlgorithmException, IOException, InvalidKeySpecException,
         OperatorCreationException, NoSuchProviderException {
    log.debug("Received agent registration request '{}'.",
        agentRegistrationRequest);

    return agentService.register(agentRegistrationRequest);
  }

  @Override
  public Response findProvisioningPackage(String hardwareId, Optional<String> token) {
    try {
      return Response.ok(agentService.findProvisioningPackage(hardwareId, token)).build();
    } catch (QLimitException e) {
      log.warn(e.getMessage());
      return Response.status(Status.TOO_MANY_REQUESTS).build();
    } catch (QSecurityException e) {
      log.warn(e.getMessage());
      return Response.status(Status.UNAUTHORIZED).build();
    }
  }

}
