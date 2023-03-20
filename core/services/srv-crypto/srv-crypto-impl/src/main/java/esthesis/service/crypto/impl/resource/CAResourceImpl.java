package esthesis.service.crypto.impl.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.slugify.Slugify;
import esthesis.common.AppConstants;
import esthesis.common.AppConstants.Audit.Category;
import esthesis.common.AppConstants.Audit.Operation;
import esthesis.common.exception.QDoesNotExistException;
import esthesis.service.audit.ccc.Audited;
import esthesis.service.audit.ccc.Audited.AuditLogType;
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
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;

@Authenticated
public class CAResourceImpl implements CAResource {

  @Inject
  CAService caService;

  @Inject
  ObjectMapper mapper;

  @GET
  @Override
  @Path("/v1/find")
  @JSONReplyFilter(filter = "content,content.id,content.cn,content.issued,content.parentCa,"
      + "content.parentCaId,content.validity,content.name")
  @Audited(cat = Category.CRYPTO, op = Operation.RETRIEVE, msg = "Search certificate authorities",
      log = AuditLogType.DATA_IN)
  public Page<CaEntity> find(@BeanParam Pageable pageable) {
    return caService.find(pageable);
  }

  @GET
  @Override
  @Path("/v1/{id}")
  @JSONReplyFilter(filter = "id,cn,issued,parentCa,validity,parentCaId,name")
  @Audited(cat = Category.CRYPTO, op = Operation.RETRIEVE, msg = "View certificate authority")
  public CaEntity findById(String id) {
    return caService.findById(id);
  }

  @GET
  @Override
  @Path("/v1/eligible-for-signing")
  @JSONReplyFilter(filter = "id,cn,name")
  public List<CaEntity> getEligbleForSigning() {
    return caService.getEligibleForSigning();
  }

  @Override
  @Audited(cat = Category.CRYPTO, op = Operation.RETRIEVE, msg = "Download certificate authority", log =
      AuditLogType.DATA_IN)
  public Response download(String caId, AppConstants.KeyType type) {
    CaEntity caEntity = caService.findById(caId);

    String content;
    String filename = Slugify.builder().underscoreSeparator(true).build()
        .slugify(caEntity.getCn());
    switch (type) {
      case PRIVATE -> {
        filename += ".key";
        content = caService.getPrivateKey(caId);
      }
      case PUBLIC -> {
        filename += ".pub";
        content = caService.getPublicKey(caId);
      }
      case CERTIFICATE -> {
        filename += ".crt";
        content = String.join("", caService.getCertificate(caId));
      }
      default -> throw new QDoesNotExistException("Key type '{}' is not valid.", type);
    }
    return ResponseBuilder.ok(content)
        .header("Content-Disposition", "attachment; filename=" + filename).build().toResponse();
  }

  @Override
  @Audited(cat = Category.CRYPTO, op = Operation.UPDATE, msg = "Import certificate authority")
  public CaEntity importCa(ImportCaForm importCaForm) {
    return caService.importCa(importCaForm);
  }

  @Override
  @Audited(cat = Category.CRYPTO, op = Operation.DELETE, msg = "Delete certificate authority")
  public void delete(String id) {
    caService.deleteById(id);
  }

  @Override
  @Audited(cat = Category.CRYPTO, op = Operation.UPDATE, msg = "Save certificate authority")
  public CaEntity save(CaEntity caEntity) {
    return caService.save(caEntity);
  }

  @Override
  public String getCACertificate(String caId) {
    return findById(caId).getCertificate();
  }
}
