package esthesis.service.kubernetes.resource;

import esthesis.service.kubernetes.dto.PodInfoDTO;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "KubernetesResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface KubernetesResource {

  @POST
  @Path("/v1/pod/start")
  Boolean schedulePod(PodInfoDTO podInfoDTO);

  @GET
  @Path("/v1/namespaces")
  List<String> getNamespaces();
}
