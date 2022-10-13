package esthesis.service.crypto.impl.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.slugify.Slugify;
import esthesis.common.exception.QMismatchException;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.PageReplyFilter;
import esthesis.service.common.paging.Pageable;
import esthesis.service.crypto.dto.Certificate;
import esthesis.service.crypto.dto.form.ImportCertificateForm;
import esthesis.service.crypto.impl.repository.CertificateRepository;
import esthesis.service.crypto.impl.service.CertificateService;
import esthesis.service.crypto.impl.service.KeyService;
import esthesis.service.crypto.resource.CertificateResource;
import esthesis.service.registry.resource.RegistryResource;
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
  RegistryResource registryResource;

  @Inject
  KeyService keyService;

  @Inject
  CertificateService certificateService;

  @Inject
  CertificateRepository certificateRepository;

  @Inject
  ObjectMapper mapper;

  @GET
  @Override
  @Path("/v1/certificate/find")
  @PageReplyFilter(filter = "content,content.id,content.cn,content.issued,content.parentCa,content.parentCaId,content.type,content.validity")
  public Page<Certificate> find(@BeanParam Pageable pageable) {
    return certificateService.find(pageable);
  }

  @GET
  @Override
  @Path("/v1/certificate/{id}")
  @PageReplyFilter(filter = "id,cn,issued,parentCa,type,validity,parentCaId")
  public Certificate findById(ObjectId id) {
    return certificateService.findById(id);
  }

  @Override
  public Response download(ObjectId caId) {
    try {
      Certificate ca = certificateService.findById(caId);
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
  public Certificate importCertificate(
      ImportCertificateForm importCertificateForm) {
    return certificateService.importCertificate(importCertificateForm);
  }

  @Override
  public void delete(ObjectId id) {
    certificateService.deleteById(id);
  }

  @Override
  public Certificate save(Certificate certificate) {
    return certificateService.save(certificate);
  }
}
