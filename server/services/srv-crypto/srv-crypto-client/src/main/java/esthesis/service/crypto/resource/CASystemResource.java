package esthesis.service.crypto.resource;

import io.quarkus.oidc.client.reactive.filter.OidcClientRequestReactiveFilter;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "CASystemResource")
@RegisterProvider(OidcClientRequestReactiveFilter.class)
public interface CASystemResource {

  @GET
  @Path("/v1/ca-system/{caId}/certificate")
  String getCACertificate(@PathParam("caId") ObjectId caId);
}
