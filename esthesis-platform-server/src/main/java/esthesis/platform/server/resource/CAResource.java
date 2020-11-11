package esthesis.platform.server.resource;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import com.eurodyn.qlack.util.data.filter.ReplyFilter;
import com.eurodyn.qlack.util.data.filter.ReplyPageableFilter;
import com.eurodyn.qlack.util.querydsl.EmptyPredicateCheck;
import com.github.slugify.Slugify;
import com.querydsl.core.types.Predicate;
import esthesis.common.util.Base64E;
import esthesis.platform.server.config.AppConstants.Cryptography.KeyType;
import esthesis.platform.server.dto.CaDTO;
import esthesis.platform.server.model.Ca;
import esthesis.platform.server.service.CAService;
import esthesis.platform.server.service.SecurityService;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
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
import java.util.List;
import java.util.Optional;

@Validated
@RestController
@RequestMapping("/cas")
public class CAResource {

  private final SecurityService securityService;
  private final CAService caService;

  public CAResource(SecurityService securityService, CAService caService) {
    this.securityService = securityService;
    this.caService = caService;
  }

  @EmptyPredicateCheck
  @ReplyPageableFilter("-certificate,-privateKey,-publicKey")
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "There was a problem retrieving CAs.")
  public Page<CaDTO> findAll(@QuerydslPredicate(root = Ca.class)Predicate predicate, Pageable pageable) {
    return caService.findAll(predicate, pageable);
  }

  @GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not fetch certificate authority.")
  @ReplyFilter("-privateKey,-publicKey")
  public CaDTO get(@PathVariable long id) {
    return caService.findById(id);
  }

  @GetMapping(path = "eligible-for-signing", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not fetch certificate authorities.")
  @ReplyFilter("-certificate,-privateKey,-publicKey")
  public List<CaDTO> getEligbleForSigning() {
    return caService.getEligibleForSigning();
  }

  @GetMapping(value = {"{id}/download/{keyType}/{base64}", "{id}/download/{keyType}"})
  @ExceptionWrapper(wrapper = QExceptionWrapper.class,
      logMessage = "Could not fetch security information for the CA.")
  public ResponseEntity download(@PathVariable long id, @PathVariable int keyType, @PathVariable Optional<Boolean> base64)
  throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException,
         InvalidAlgorithmParameterException,
         IOException {
    final CaDTO caDTO = caService.findById(id);

    String filename = new Slugify().slugify(caDTO.getCn());
    String body = "";
    switch (keyType) {
      case KeyType.CERTIFICATE:
        filename += ".crt";
        body = caDTO.getCertificate();
        break;
      case KeyType.PUBLIC_KEY:
        filename += ".pem";
        body = caDTO.getPublicKey();
        break;
      case KeyType.PRIVATE_KEY:
        filename += ".key";
        body = new String(securityService.decrypt(caDTO.getPrivateKey()), StandardCharsets.UTF_8);
        break;
    }

    if (base64.isPresent() && base64.get().booleanValue()) {
      body = Base64E.encode(body.getBytes(StandardCharsets.UTF_8));
      filename += ".base64";
    }

    return ResponseEntity
        .ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(body);
  }

  @GetMapping(value = "{id}/backup")
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not create backup for CA.")
  public ResponseEntity backup(@PathVariable long id)
  throws IOException, NoSuchPaddingException, InvalidAlgorithmParameterException,
         NoSuchAlgorithmException, InvalidKeyException {
    final CaDTO caDTO = caService.findById(id);
    return ResponseEntity
        .ok()
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=" + new Slugify().slugify(caDTO.getCn()) + ".backup")
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(caService.backup(id));
  }

  @PostMapping(value = "/restore")
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not restore CA.")
  public ResponseEntity restore(@NotNull @RequestParam("backup") MultipartFile backup)
  throws IOException, NoSuchPaddingException, InvalidAlgorithmParameterException,
         NoSuchAlgorithmException, InvalidKeyException {
    caService.restore(IOUtils.toString(backup.getInputStream(), StandardCharsets.UTF_8));

    return ResponseEntity.ok().build();
  }

  @DeleteMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not delete application.")
  public void delete(@PathVariable long id) {
    caService.deleteById(id);
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not save Certificate Authority.")
  @ReplyFilter("-privateKey,-publicKey,-certificate,-createdBy,-createdOn")
  public CaDTO save(@Valid @RequestBody CaDTO object) {
    return caService.save(object);
  }

}
