package esthesis.service.dataflow.impl.resource;

import esthesis.service.common.paging.JSONReplyFilter;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.dataflow.dto.DockerTagsDTO;
import esthesis.service.dataflow.dto.MatchedMqttServerDTO;
import esthesis.service.dataflow.entity.DataflowEntity;
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
  @JSONReplyFilter(filter = "content,content.id,content.name,content.type,"
      + "content.status,content.description")
  public Page<DataflowEntity> find(@BeanParam Pageable pageable) {
    return dataflowService.find(pageable, true);
  }

  @Override
  public DataflowEntity findById(ObjectId id) {
    return dataflowService.findById(id);
  }

  @Override
  public Response delete(ObjectId id) {
    dataflowService.deleteById(id);
    return Response.ok().build();
  }

  @Override
  public DataflowEntity save(DataflowEntity dataflowEntity) {
    return dataflowService.save(dataflowEntity);
  }

  @Override
  public MatchedMqttServerDTO matchMqttServerByTags(List<String> tags) {
    return dataflowService.matchMqttServerByTags(tags);
  }

  @Override
  public DockerTagsDTO getImageTags(String dflType) {
    return dataflowService.getImageTags(dflType);
  }

  @Override
  public List<String> getNamespaces() {
    return dataflowService.getNamespaces();
  }

}
