package esthesis.dataflows.oriongateway.client;

import esthesis.dataflow.common.RestClientExceptionMapper;
import esthesis.dataflows.oriongateway.dto.OrionQueryDTO;
import java.util.List;
import java.util.Map;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("")
@Produces("application/json")
@RegisterRestClient(configKey = "OrionClient")
@RegisterProvider(RestClientExceptionMapper.class)
public interface OrionClient {

  @GET
  @Path("/version")
  @Retry(maxRetries = 3)
  String getVersion();

//  @GET
//  @Path("/v2/entities/{entityId}")
//  @Retry(maxRetries = 3)
//  List<Map<String, Object>> getEntity(@PathParam("entityId") String entityId);

  @POST
  @Path("/v2/entities")
  @Retry(maxRetries = 3)
  void createEntity(String json);

  @POST
  @Path("/v2/entities/{entityId}/attrs")
  @Retry(maxRetries = 3)
  void setAttribute(@PathParam("entityId") String entityId, String json);

  @DELETE
  @Path("/v2/entities/{entityId}/attrs/{attrName}")
  @Retry(maxRetries = 3)
  void deleteAttribute(@PathParam("entityId") String entityId,
      @PathParam("attrName") String attrName);

  @DELETE
  @Path("/v2/entities/{entityId}")
  @Retry(maxRetries = 3)
  void deleteEntity(@PathParam("entityId") String entityId);

  @POST
  @Path("/v2/op/query")
  @Retry(maxRetries = 3)
  List<Map<String, Object>> query(OrionQueryDTO orionQueryDTO);
}
