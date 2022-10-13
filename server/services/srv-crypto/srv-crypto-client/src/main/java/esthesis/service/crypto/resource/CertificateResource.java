package esthesis.service.crypto.resource;

import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.crypto.dto.Certificate;
import esthesis.service.crypto.dto.form.ImportCertificateForm;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.MultipartForm;

@Path("/api")
@RegisterRestClient(configKey = "CertificateResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface CertificateResource {

  @GET
  @Path("/v1/certificate/find")
  Page<Certificate> find(@BeanParam Pageable pageable);

  @GET
  @Path("/v1/certificate/{id}")
  Certificate findById(@PathParam("id") ObjectId id);

  @GET
  @Path("/v1/certificate/{id}/download")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  Response download(@PathParam("id") ObjectId id);

  @POST
  @Path("/v1/certificate/import")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  Certificate importCertificate(@MultipartForm ImportCertificateForm input);

  @DELETE
  @Path("/v1/certificate/{id}")
  void delete(@PathParam("id") ObjectId id);

  @POST
  @Path("/v1/certificate")
  Certificate save(@Valid Certificate object);

//  @EmptyPredicateCheck
//  @ReplyPageableFilter("-certificate,-privateKey,-publicKey")
//  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
//  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "There was a problem retrieving certificates.")
//  public Page<CertificateDTO> findAll(
//      @QuerydslPredicate(root = Certificate.class) Predicate predicate,
//      Pageable pageable) {
//    return certificatesService.findAll(predicate, pageable);
//  }
//
//  @GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
//  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not fetch certificate.")
//  public CertificateDTO get(@PathVariable long id) {
//    return certificatesService.findById(id);
//  }

//  @GetMapping(value = {"{id}/download/{keyType}/{base64}", "{id}/download/{keyType}"})
//  @ExceptionWrapper(wrapper = QExceptionWrapper.class,
//      logMessage = "Could not download certificate.")
//  public ResponseEntity download(@PathVariable long id, @PathVariable int keyType,
//      @PathVariable Optional<Boolean> base64)
//  throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException,
//         InvalidKeyException, IOException {
//    DownloadReply keyDownloadReply = certificatesService
//        .download(id, keyType, base64.isPresent() && base64.get().booleanValue());
//    return ResponseEntity
//        .ok()
//        .header(HttpHeaders.CONTENT_DISPOSITION,
//            "attachment; filename=" + keyDownloadReply.getFilename())
//        .contentType(MediaType.APPLICATION_OCTET_STREAM)
//        .body(keyDownloadReply.getPayload());
//  }
//
//  @DeleteMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
//  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not delete certificate.")
//  public void delete(@PathVariable long id) {
//    certificatesService.deleteById(id);
//  }
//
//  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not save certificate.")
//  @ReplyFilter("-privateKey,-publicKey,-certificate,-createdBy,-createdOn")
//  public CertificateDTO save(@Valid @RequestBody CertificateDTO certificateDTO) {
//    return certificatesService.save(certificateDTO);
//  }
//
//  @PostMapping(value = "/restore")
//  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not restore "
//      + "certificate.")
//  public ResponseEntity restore(@NotNull @RequestParam("backup") MultipartFile backup)
//  throws IOException, NoSuchPaddingException, InvalidAlgorithmParameterException,
//         NoSuchAlgorithmException, InvalidKeyException {
//    certificatesService.restore(IOUtils.toString(backup.getInputStream(), StandardCharsets.UTF_8));
//
//    return ResponseEntity.ok().build();
//  }
//
//  @GetMapping(value = "{id}/backup")
//  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not create backup for "
//      + "certificate.")
//  public ResponseEntity backup(@PathVariable long id)
//  throws IOException, NoSuchPaddingException, InvalidAlgorithmParameterException,
//         NoSuchAlgorithmException, InvalidKeyException {
//    final CertificateDTO certificateDTO = certificatesService.findById(id);
//    return ResponseEntity
//        .ok()
//        .header(HttpHeaders.CONTENT_DISPOSITION,
//            "attachment; filename=" + new Slugify().slugify(certificateDTO.getCn()) + ".backup")
//        .contentType(MediaType.APPLICATION_OCTET_STREAM)
//        .body(certificatesService.backup(id));
//  }

}
