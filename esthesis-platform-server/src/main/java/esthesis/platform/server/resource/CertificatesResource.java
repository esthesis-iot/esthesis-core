package esthesis.platform.server.resource;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import com.eurodyn.qlack.util.data.filter.ReplyPageableFilter;
import com.eurodyn.qlack.util.querydsl.EmptyPredicateCheck;
import com.querydsl.core.types.Predicate;
import esthesis.platform.server.dto.CertificateDTO;
import esthesis.platform.server.dto.KeyDownloadReply;
import esthesis.platform.server.model.Certificate;
import esthesis.platform.server.service.CertificatesService;
import esthesis.platform.server.service.SecurityService;
import javax.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@Validated
@RequestMapping("/certificates")
public class CertificatesResource {

  private final SecurityService securityService;
  private final CertificatesService certificatesService;

  public CertificatesResource(SecurityService securityService,
      CertificatesService certificatesService) {
    this.securityService = securityService;

    this.certificatesService = certificatesService;
  }

  @EmptyPredicateCheck
  @ReplyPageableFilter("-certificate,-privateKey,-publicKey")
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "There was a problem retrieving certificates.")
  public Page<CertificateDTO> findAll(@QuerydslPredicate(root = Certificate.class) Predicate predicate,
      Pageable pageable) {
    return certificatesService.findAll(predicate, pageable);
  }

  @GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not fetch certificate authority.")
  public CertificateDTO get(@PathVariable long id) {
    return certificatesService.findById(id);
  }

  @GetMapping(value = {"{id}/download/{keyType}/{base64}", "{id}/download/{keyType}"})
  @ExceptionWrapper(wrapper = QExceptionWrapper.class,
      logMessage = "Could not fetch security information for the certificate.")
  public ResponseEntity download(@PathVariable long id, @PathVariable int keyType,
      @PathVariable Optional<Boolean> base64) {
    KeyDownloadReply keyDownloadReply = certificatesService
        .download(id, keyType, base64.isPresent() && base64.get().booleanValue());
    return ResponseEntity
        .ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + keyDownloadReply.getFilename())
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(keyDownloadReply.getPayload());
  }

  //  @GetMapping(value = "{id}/backup")
  //  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not create backup for CA.")
  //  public ResponseEntity backup(@PathVariable long id) throws IOException {
  //    final CaDTO caDTO = caService.findById(id);
  //    return ResponseEntity
  //        .ok()
  //        .header(HttpHeaders.CONTENT_DISPOSITION,
  //            "attachment; filename=" + new Slugify().slugify(caDTO.getCn()) + ".backup")
  //        .contentType(MediaType.APPLICATION_OCTET_STREAM)
  //        .body(caService.backup(id));
  //  }

  //  @PostMapping(value = "/restore")
  //  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not restore CA.")
  //  public ResponseEntity restore(@NotNull @RequestParam("backup") MultipartFile backup) throws IOException {
  //    caService.restore(IOUtils.toString(backup.getInputStream(), StandardCharsets.UTF_8));
  //
  //    return ResponseEntity.ok().build();
  //  }

  @DeleteMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not delete application.")
  public void delete(@PathVariable long id) {
    certificatesService.deleteById(id);
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not save Certificate Authority.")
  public CertificateDTO save(@Valid @RequestBody CertificateDTO object) {
    return certificatesService.save(object);
  }

}
