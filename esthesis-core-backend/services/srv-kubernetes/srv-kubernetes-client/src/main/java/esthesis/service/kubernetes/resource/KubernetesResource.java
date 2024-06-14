package esthesis.service.kubernetes.resource;

import esthesis.service.kubernetes.dto.PodInfoDTO;
import io.quarkus.oidc.token.propagation.AccessToken;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@AccessToken
@Path("/api")
@RegisterRestClient(configKey = "KubernetesResource")
public interface KubernetesResource {

	@POST
	@Path("/v1/pod/start")
	Boolean schedulePod(PodInfoDTO podInfoDTO);

	@GET
	@Path("/v1/namespaces")
	List<String> getNamespaces();
}
