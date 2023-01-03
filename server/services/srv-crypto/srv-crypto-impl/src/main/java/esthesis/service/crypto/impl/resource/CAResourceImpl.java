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
  @Path("/v1/find")
  @JSONReplyFilter(filter = "content,content.id,content.cn,content.issued,content.parentCa,"
      + "content.parentCaId,content.validity,content.name")
  @Audited(cat = Category.CRYPTO, op = Operation.READ, msg = "Search certificate authorities",
      log = AuditLogType.DATA_IN)
  public Page<CaEntity> find(@BeanParam Pageable pageable) {
    return caService.find(pageable);
  }

  @GET
  @Override
  @Path("/v1/{id}")
  @JSONReplyFilter(filter = "id,cn,issued,parentCa,validity,parentCaId,name")
  @Audited(cat = Category.CRYPTO, op = Operation.READ, msg = "View certificate authority")
  public CaEntity findById(ObjectId id) {
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
  @Audited(cat = Category.CRYPTO, op = Operation.READ, msg = "Download certificate authority")
  public Response download(ObjectId caId, AppConstants.KeyType type) {
    CaEntity caEntity = caService.findById(caId);

    String content;
    String filename = Slugify.builder().underscoreSeparator(true).build()
        .slugify(caEntity.getCn());
    switch (type) {
      case PRIVATE -> {
        filename += ".key";
        content = caEntity.getPrivateKey();
      }
      case PUBLIC -> {
        filename += ".pub";
        content = caEntity.getPublicKey();
      }
      case CERTIFICATE -> {
        filename += ".crt";
        content = caEntity.getCertificate();
      }
      default -> throw new QDoesNotExistException("Key type {} is not valid.", type);
    }
    return ResponseBuilder.ok(content)
        .header("Content-Disposition", "attachment; filename=" + filename).build().toResponse();
  }

  @Override
  @Audited(cat = Category.CRYPTO, op = Operation.WRITE, msg = "Import certificate authority")
  public CaEntity importCa(ImportCaForm importCaForm) {
    return caService.importCa(importCaForm);
  }

  @Override
  @Audited(cat = Category.CRYPTO, op = Operation.DELETE, msg = "Delete certificate authority")
  public void delete(ObjectId id) {
    caService.deleteById(id);
  }

  @Override
  @Audited(cat = Category.CRYPTO, op = Operation.WRITE, msg = "Save certificate authority")
  public CaEntity save(CaEntity caEntity) {
    return caService.save(caEntity);
  }

  @Override
  public String getCACertificate(ObjectId caId) {
    return findById(caId).getCertificate();
  }
}
