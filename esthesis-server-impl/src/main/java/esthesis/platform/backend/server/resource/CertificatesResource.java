package esthesis.platform.backend.server.resource;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import com.eurodyn.qlack.util.data.filter.ReplyFilter;
import com.eurodyn.qlack.util.data.filter.ReplyPageableFilter;
import com.eurodyn.qlack.util.querydsl.EmptyPredicateCheck;
import com.github.slugify.Slugify;
import com.querydsl.core.types.Predicate;
import esthesis.platform.backend.server.dto.CertificateDTO;
import esthesis.platform.backend.server.dto.DownloadReply;
import esthesis.platform.backend.server.model.Certificate;
import esthesis.platform.backend.server.service.CertificatesService;
import javax.crypto.NoSuchPaddingException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.apache.commons.io.IOUtils;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@RestController
@Validated
@RequestMapping("/certificates")
public class CertificatesResource {

  private final CertificatesService certificatesService;

  public CertificatesResource(CertificatesService certificatesService) {
    this.certificatesService = certificatesService;
  }

  @EmptyPredicateCheck
  @ReplyPageableFilter("-certificate,-privateKey,-publicKey")
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "There was a problem retrieving certificates.")
  public Page<CertificateDTO> findAll(
    @QuerydslPredicate(root = Certificate.class) Predicate predicate,
    Pageable pageable) {
    return certificatesService.findAll(predicate, pageable);
  }

  @GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not fetch certificate.")
  public CertificateDTO get(@PathVariable long id) {
    return certificatesService.findById(id);
  }

  @GetMapping(value = {"{id}/download/{keyType}/{base64}", "{id}/download/{keyType}"})
  @ExceptionWrapper(wrapper = QExceptionWrapper.class,
    logMessage = "Could not download certificate.")
  public ResponseEntity download(@PathVariable long id, @PathVariable int keyType,
    @PathVariable Optional<Boolean> base64)
    throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException,
    InvalidKeyException, IOException {
    DownloadReply keyDownloadReply = certificatesService
      .download(id, keyType, base64.isPresent() && base64.get().booleanValue());
    return ResponseEntity
      .ok()
      .header(HttpHeaders.CONTENT_DISPOSITION,
        "attachment; filename=" + keyDownloadReply.getFilename())
      .contentType(MediaType.APPLICATION_OCTET_STREAM)
      .body(keyDownloadReply.getPayload());
  }

  @DeleteMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not delete certificate.")
  public void delete(@PathVariable long id) {
    certificatesService.deleteById(id);
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not save certificate.")
  @ReplyFilter("-privateKey,-publicKey,-certificate,-createdBy,-createdOn")
  public CertificateDTO save(@Valid @RequestBody CertificateDTO certificateDTO) {
    return certificatesService.save(certificateDTO);
  }

  @PostMapping(value = "/restore")
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not restore "
    + "certificate.")
  public ResponseEntity restore(@NotNull @RequestParam("backup") MultipartFile backup)
    throws IOException, NoSuchPaddingException, InvalidAlgorithmParameterException,
    NoSuchAlgorithmException, InvalidKeyException {
    certificatesService.restore(IOUtils.toString(backup.getInputStream(), StandardCharsets.UTF_8));

    return ResponseEntity.ok().build();
  }

  @GetMapping(value = "{id}/backup")
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not create backup for "
    + "certificate.")
  public ResponseEntity backup(@PathVariable long id)
    throws IOException, NoSuchPaddingException, InvalidAlgorithmParameterException,
    NoSuchAlgorithmException, InvalidKeyException {
    final CertificateDTO certificateDTO = certificatesService.findById(id);
    return ResponseEntity
      .ok()
      .header(HttpHeaders.CONTENT_DISPOSITION,
        "attachment; filename=" + new Slugify().slugify(certificateDTO.getCn()) + ".backup")
      .contentType(MediaType.APPLICATION_OCTET_STREAM)
      .body(certificatesService.backup(id));
  }

}
