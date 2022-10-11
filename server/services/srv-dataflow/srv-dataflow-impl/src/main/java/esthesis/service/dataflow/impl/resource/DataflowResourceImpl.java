package esthesis.service.dataflow.impl.resource;

import esthesis.common.rest.Page;
import esthesis.common.rest.PageReplyFilter;
import esthesis.common.rest.Pageable;
import esthesis.service.dataflow.dto.Dataflow;
import esthesis.service.dataflow.dto.DockerTags;
import esthesis.service.dataflow.dto.MatchedMqttServer;
import esthesis.service.dataflow.impl.service.DataflowService;
import esthesis.service.dataflow.resource.DataflowResource;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.bson.types.ObjectId;

public class DataflowResourceImpl implements DataflowResource {

  @Inject
  DataflowService dataflowService;

  @GET
  @Override
  @Path("/v1/dataflow/find")
  @PageReplyFilter(filter = "content,content.id,content.name,content.type,"
      + "content.status,content.description")
  public Page<Dataflow> find(@BeanParam Pageable pageable) {
    return dataflowService.find(pageable, true);
  }

  @Override
  public Dataflow findById(ObjectId id) {
    return dataflowService.findById(id);
  }

  @Override
  public Response delete(ObjectId id) {
    dataflowService.deleteById(id);
    return Response.ok().build();
  }

  @Override
  public Dataflow save(Dataflow dataflow) {
    return dataflowService.save(dataflow);
  }

  @Override
  public MatchedMqttServer matchMqttServerByTags(List<String> tags) {
    return dataflowService.matchMqttServerByTags(tags);
  }

  @Override
  public DockerTags getImageTags(String dflType) {
    return dataflowService.getImageTags(dflType);
  }

  @Override
  public List<String> getNamespaces() {
    return dataflowService.getNamespaces();
  }

}
