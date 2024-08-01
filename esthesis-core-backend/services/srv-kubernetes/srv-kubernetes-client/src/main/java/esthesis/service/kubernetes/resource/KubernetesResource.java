package esthesis.service.kubernetes.resource;

import esthesis.service.kubernetes.dto.DeploymentInfoDTO;
import io.quarkus.oidc.token.propagation.AccessToken;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@AccessToken
@Path("/api")
@RegisterRestClient(configKey = "KubernetesResource")
public interface KubernetesResource {

	@POST
	@Path("/v1/deployment/schedule")
	Boolean scheduleDeployment(DeploymentInfoDTO deploymentInfoDTO);

	@GET
	@Path("/v1/namespaces")
	List<String> getNamespaces();

	@GET
	@Path("/v1/deployment/check-name-available")
	Boolean isDeploymentNameAvailable(@QueryParam("name") String name,
		@QueryParam("namespace") String namespace);
}
