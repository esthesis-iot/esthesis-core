package esthesis.service.crypto.impl.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.slugify.Slugify;
import esthesis.common.AppConstants;
import esthesis.common.exception.QDoesNotExistException;
import esthesis.service.common.paging.JSONReplyFilter;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.crypto.entity.CertificateEntity;
import esthesis.service.crypto.form.ImportCertificateForm;
import esthesis.service.crypto.impl.repository.CertificateEntityRepository;
import esthesis.service.crypto.impl.service.CertificateService;
import esthesis.service.crypto.resource.CertificateResource;
import esthesis.service.settings.resource.SettingsResource;
import io.quarkus.security.Authenticated;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;

@Authenticated
public class CertificateResourceImpl implements CertificateResource {

  @Inject
  JsonWebToken jwt;

  @Inject
  @RestClient
  SettingsResource settingsResource;

  @Inject
  CertificateService certificateService;

  @Inject
  CertificateEntityRepository certificateEntityRepository;

  @Inject
  ObjectMapper mapper;

  @GET
  @Override
  @Path("/v1/certificate/find")
  @JSONReplyFilter(filter = "content,content.id,content.cn,content.issued,content.issuer,content"
      + ".validity,content.name")
  public Page<CertificateEntity> find(@BeanParam Pageable pageable) {
    return certificateService.find(pageable);
  }

  @GET
  @Override
  @Path("/v1/certificate/{id}")
  @JSONReplyFilter(filter = "id,cn,issued,validity,issuer,san,name")
  public CertificateEntity findById(ObjectId id) {
    return certificateService.findById(id);
  }

  @Override
  public Response download(ObjectId certId, AppConstants.KeyType type) {
    CertificateEntity cert = certificateService.findById(certId);

    String content;
    String filename = Slugify.builder().underscoreSeparator(true).build().slugify(cert.getCn());
    switch (type) {
      case PRIVATE -> {
        filename += ".key";
        content = cert.getPrivateKey();
      }
      case PUBLIC -> {
        filename += ".pub";
        content = cert.getPublicKey();
      }
      case CERTIFICATE -> {
        filename += ".crt";
        content = cert.getCertificate();
      }
      default -> throw new QDoesNotExistException("Key type {} is not valid.", type);
    }
    return ResponseBuilder.ok(content)
        .header("Content-Disposition", "attachment; filename=" + filename).build().toResponse();
  }

  @Override
  public CertificateEntity importCertificate(ImportCertificateForm importCertificateForm) {
    return certificateService.importCertificate(importCertificateForm);
  }

  @Override
  public void delete(ObjectId id) {
    certificateService.deleteById(id);
  }

  @Override
  public CertificateEntity save(CertificateEntity certificateEntity) {
    return certificateService.save(certificateEntity);
  }
}
