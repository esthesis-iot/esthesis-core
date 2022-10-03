package esthesis.service.kubernetes.resource;

import esthesis.service.kubernetes.dto.PodInfo;
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
  @Path("/v1/kubernetes/pod/start")
  Boolean startPod(PodInfo podInfo);

  @GET
  @Path("/v1/kubernetes/namespaces")
  List<String> getNamespaces();
}
