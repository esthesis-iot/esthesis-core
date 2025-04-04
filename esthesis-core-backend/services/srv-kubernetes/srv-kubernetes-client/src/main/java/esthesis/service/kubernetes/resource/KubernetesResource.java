package esthesis.service.kubernetes.resource;

import esthesis.service.kubernetes.dto.DeploymentInfoDTO;
import io.quarkus.oidc.token.propagation.common.AccessToken;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * REST client for the Kubernetes service.
 */
@AccessToken
@Path("/api")
@RegisterRestClient(configKey = "KubernetesResource")
public interface KubernetesResource {

	/**
	 * Schedule a deployment.
	 *
	 * @param deploymentInfoDTO the deployment info.
	 * @return true if the deployment was scheduled, false otherwise.
	 */
	@POST
	@Path("/v1/deployment/schedule")
	Boolean scheduleDeployment(DeploymentInfoDTO deploymentInfoDTO);

	/**
	 * Get the list of namespaces.
	 *
	 * @return the list of namespaces.
	 */
	@GET
	@Path("/v1/namespaces")
	List<String> getNamespaces();

	/**
	 * Check if a deployment name is available.
	 *
	 * @param name      the deployment name.
	 * @param namespace the namespace.
	 * @return true if the name is available, false otherwise.
	 */
	@GET
	@Path("/v1/deployment/check-name-available")
	Boolean isDeploymentNameAvailable(@QueryParam("name") String name,
		@QueryParam("namespace") String namespace);
}
