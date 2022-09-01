package esthesis.service.crypto.impl.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.slugify.Slugify;
import esthesis.common.exception.QMismatchException;
import esthesis.common.rest.Page;
import esthesis.common.rest.PageReplyFilter;
import esthesis.common.rest.Pageable;
import esthesis.service.crypto.dto.Ca;
import esthesis.service.crypto.dto.form.ImportCaForm;
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
  @PageReplyFilter(filter = "content,content.id,content.cn,content.issued,content.parentCa,content.parentCaId,content.type,content.validity")
  public Page<Ca> find(@BeanParam Pageable pageable) {
    return caService.find(pageable);
  }

  @GET
  @Override
  @Path("/v1/ca/{id}")
  @PageReplyFilter(filter = "id,cn,issued,parentCa,type,validity,parentCaId")
  public Ca findById(ObjectId id) {
    return caService.findById(id);
  }

  @GET
  @Override
  @Path("/v1/ca/eligible-for-signing")
  @PageReplyFilter(filter = "id,cn")
  public List<Ca> getEligbleForSigning() {
    return caService.getEligibleForSigning();
  }

  @Override
  public Response download(ObjectId caId) {
    try {
      Ca ca = caService.findById(caId);
      String filename = Slugify.builder().underscoreSeparator(true).build()
          .slugify(ca.getCn());
      return ResponseBuilder.ok(mapper.writeValueAsString(findById(caId)))
          .header("Content-Disposition",
              "attachment; filename=" + filename + ".yaml").build()
          .toResponse();
    } catch (JsonProcessingException e) {
      throw new QMismatchException("Could not fetch CA with id '{}'.", caId);
    }
  }

  @Override
  public Ca importCa(ImportCaForm importCaForm) {
    return caService.importCa(importCaForm);
  }

  @Override
  public void delete(ObjectId id) {
    caService.deleteById(id);
  }

  @Override
  public Ca save(Ca ca) {
    return caService.save(ca);
  }

  @Override
  public String getCACertificate(ObjectId caId) {
    return findById(caId).getCertificate();
  }
}
