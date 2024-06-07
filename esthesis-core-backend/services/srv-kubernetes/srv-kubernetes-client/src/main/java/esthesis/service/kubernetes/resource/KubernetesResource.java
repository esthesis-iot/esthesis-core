package esthesis.service.kubernetes.resource;

import esthesis.common.AppConstants;
import esthesis.service.kubernetes.dto.PodInfoDTO;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import java.util.List;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "KubernetesResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface KubernetesResource {

	@POST
	@Path("/v1/pod/start")
	@RolesAllowed(AppConstants.ROLE_USER)
	Boolean schedulePod(PodInfoDTO podInfoDTO);

	@GET
	@Path("/v1/namespaces")
	@RolesAllowed(AppConstants.ROLE_USER)
	List<String> getNamespaces();
}
