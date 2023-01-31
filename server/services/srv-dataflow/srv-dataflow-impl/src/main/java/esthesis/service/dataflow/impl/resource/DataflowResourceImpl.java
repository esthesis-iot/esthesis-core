package esthesis.service.dataflow.impl.resource;

import esthesis.common.AppConstants.Audit.Category;
import esthesis.common.AppConstants.Audit.Operation;
import esthesis.service.audit.ccc.Audited;
import esthesis.service.audit.ccc.Audited.AuditLogType;
import esthesis.service.common.paging.JSONReplyFilter;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.dataflow.dto.DockerTagsDTO;
import esthesis.service.dataflow.entity.DataflowEntity;
import esthesis.service.dataflow.impl.service.DataflowService;
import esthesis.service.dataflow.resource.DataflowResource;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

public class DataflowResourceImpl implements DataflowResource {

  @Inject
  DataflowService dataflowService;

  @GET
  @Override
  @Path("/v1/find")
  @JSONReplyFilter(filter = "content,content.id,content.name,content.type,"
      + "content.status,content.description")
  @Audited(cat = Category.DATAFLOW, op = Operation.READ, msg = "Search dataflows",
      log = AuditLogType.DATA_IN)
  public Page<DataflowEntity> find(@BeanParam Pageable pageable) {
    return dataflowService.find(pageable, true);
  }

  @Override
  @Audited(cat = Category.DATAFLOW, op = Operation.READ, msg = "View dataflow")
  public DataflowEntity findById(String id) {
    return dataflowService.findById(id);
  }

  @Override
  @Audited(cat = Category.DATAFLOW, op = Operation.DELETE, msg = "Delete dataflow")
  public Response delete(String dataflowId) {
    dataflowService.delete(dataflowId);
    return Response.ok().build();
  }

  @Override
  @Audited(cat = Category.DATAFLOW, op = Operation.WRITE, msg = "Save dataflow")
  public DataflowEntity save(DataflowEntity dataflowEntity) {
    return dataflowService.save(dataflowEntity);
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
