package esthesis.platform.server.resource;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import com.eurodyn.qlack.util.data.filter.ReplyPageableFilter;
import com.eurodyn.qlack.util.querydsl.EmptyPredicateCheck;
import com.querydsl.core.types.Predicate;
import esthesis.platform.server.dto.ProvisioningDTO;
import esthesis.platform.server.model.Provisioning;
import esthesis.platform.server.service.ProvisioningService;
import javax.validation.constraints.NotNull;
import org.apache.commons.io.IOUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
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

@RestController
@Validated
@RequestMapping("/provisioning")
public class ProvisioningResource {

  private final ProvisioningService provisioningService;

  public ProvisioningResource(
    ProvisioningService provisioningPackageService) {
    this.provisioningService = provisioningPackageService;
  }

  @PostMapping
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not save provisioning package.")
  public ResponseEntity save(@NotNull @RequestParam("file") MultipartFile file,
    @ModelAttribute ProvisioningDTO provisioningDTO) throws Exception {
    provisioningDTO.setFileSize(file.getSize());
    provisioningDTO.setFileContent(IOUtils.toByteArray(file.getInputStream()));
    provisioningDTO.setFileName(file.getOriginalFilename());
    provisioningService.save(provisioningDTO);

    return ResponseEntity.ok().build();
  }

  @GetMapping
  @EmptyPredicateCheck
  @ReplyPageableFilter("defaultIP,fileSize,id,name,packageVersion,state")
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
}
