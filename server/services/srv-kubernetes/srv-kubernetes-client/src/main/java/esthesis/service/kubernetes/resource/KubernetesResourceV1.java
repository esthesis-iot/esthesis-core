package esthesis.service.kubernetes.resource;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/api/v1/kubernetes")
public interface KubernetesResourceV1 {

  @POST
  @Path("/pod/start")
  Response startPod();
}
