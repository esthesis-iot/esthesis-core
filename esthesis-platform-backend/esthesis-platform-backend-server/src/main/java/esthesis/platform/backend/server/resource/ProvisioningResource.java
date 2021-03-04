package esthesis.platform.backend.server.resource;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import com.eurodyn.qlack.util.data.filter.ReplyPageableFilter;
import com.eurodyn.qlack.util.querydsl.EmptyPredicateCheck;
import com.querydsl.core.types.Predicate;
import esthesis.platform.backend.server.dto.ProvisioningDTO;
import esthesis.platform.backend.server.model.Provisioning;
import esthesis.platform.backend.server.service.ProvisioningService;
import javax.crypto.NoSuchPaddingException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;

@RestController
@Validated
@RequestMapping("/provisioning")
public class ProvisioningResource {

  private final ProvisioningService provisioningService;

  public ProvisioningResource(ProvisioningService provisioningPackageService) {
    this.provisioningService = provisioningPackageService;
  }

  @PostMapping
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not save provisioning package.")
  public ResponseEntity save(@RequestParam("file") Optional<MultipartFile> file,
    @ModelAttribute ProvisioningDTO provisioningDTO)
    throws IOException, NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException,
    SignatureException, InvalidAlgorithmParameterException, InvalidKeySpecException {
    if (provisioningDTO.getId() == null && file.isPresent()) {
      provisioningService.save(provisioningDTO, file.get());
    } else {
      // Make sure existing r/o attributes are not overwritten.
      @SuppressWarnings("ConstantConditions") final Provisioning existingProvisioning =
        provisioningService.findEntityById(provisioningDTO.getId());
      provisioningDTO.setFileSize(existingProvisioning.getFileSize());
      provisioningDTO.setFileName(existingProvisioning.getFileName());
      provisioningDTO.setSha256(existingProvisioning.getSha256());
      provisioningService.save(provisioningDTO);
    }

    return ResponseEntity.ok().build();
  }

  @GetMapping
  @EmptyPredicateCheck
  @ReplyPageableFilter("fileSize,id,name,packageVersion,state,tags,fileName,createdOn,"
    + "description,signed,encrypted")
  public Page<ProvisioningDTO> findAll(
    @QuerydslPredicate(root = Provisioning.class) Predicate predicate, Pageable pageable) {
    return provisioningService.findAll(predicate, pageable);
  }

  @GetMapping(value = "{id}")
  public ProvisioningDTO get(@PathVariable long id) {
    return provisioningService.findById(id);
  }

  @DeleteMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not delete "
    + "provisioning package.")
  public void delete(@PathVariable long id) {
    provisioningService.deleteById(id);
  }

  @GetMapping(value = "{id}/download")
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not download "
    + "provisioning package.")
  public ResponseEntity download(@PathVariable long id) throws IOException {
    final ProvisioningDTO provisioningDTO = provisioningService.findById(id);

    return ResponseEntity
      .ok()
      .header(HttpHeaders.CONTENT_DISPOSITION,
        "attachment; filename=" + provisioningDTO.getFileName())
      .contentLength(provisioningDTO.getFileSize())
      .contentType(MediaType.APPLICATION_OCTET_STREAM)
      .body(new InputStreamResource(provisioningService.download(id)));
  }
}
