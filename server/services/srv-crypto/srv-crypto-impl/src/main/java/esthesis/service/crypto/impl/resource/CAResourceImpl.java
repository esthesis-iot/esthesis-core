package esthesis.service.crypto.impl.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.slugify.Slugify;
import esthesis.common.exception.QMismatchException;
import esthesis.service.common.paging.JSONReplyFilter;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.crypto.entity.CaEntity;
import esthesis.service.crypto.form.ImportCaForm;
import esthesis.service.crypto.impl.service.CAService;
import esthesis.service.crypto.resource.CAResource;
import io.quarkus.security.Authenticated;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.bson.types.ObjectId;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;

@Authenticated
public class CAResourceImpl implements CAResource {

  @Inject
  CAService caService;

  @Inject
  ObjectMapper mapper;

  @GET
  @Override
  @Path("/v1/ca/find")
  @JSONReplyFilter(filter = "content,content.id,content.cn,content.issued,content.parentCa,content.parentCaId,content.type,content.validity")
  public Page<CaEntity> find(@BeanParam Pageable pageable) {
    return caService.find(pageable);
  }

  @GET
  @Override
  @Path("/v1/ca/{id}")
  @JSONReplyFilter(filter = "id,cn,issued,parentCa,type,validity,parentCaId")
  public CaEntity findById(ObjectId id) {
    return caService.findById(id);
  }

  @GET
  @Override
  @Path("/v1/ca/eligible-for-signing")
  @JSONReplyFilter(filter = "id,cn")
  public List<CaEntity> getEligbleForSigning() {
    return caService.getEligibleForSigning();
  }

  @Override
  public Response download(ObjectId caId) {
    try {
      CaEntity caEntity = caService.findById(caId);
      String filename = Slugify.builder().underscoreSeparator(true).build()
          .slugify(caEntity.getCn());
      return ResponseBuilder.ok(mapper.writeValueAsString(findById(caId)))
          .header("Content-Disposition",
              "attachment; filename=" + filename + ".yaml").build()
          .toResponse();
    } catch (JsonProcessingException e) {
      throw new QMismatchException("Could not fetch CA with id '{}'.", caId);
    }
  }

  @Override
  public CaEntity importCa(ImportCaForm importCaForm) {
    return caService.importCa(importCaForm);
  }

  @Override
  public void delete(ObjectId id) {
    caService.deleteById(id);
  }

  @Override
  public CaEntity save(CaEntity caEntity) {
    return caService.save(caEntity);
  }

  @Override
  public String getCACertificate(ObjectId caId) {
    return findById(caId).getCertificate();
  }
}
