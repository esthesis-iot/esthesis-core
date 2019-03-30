package esthesis.platform.server.resource;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.util.data.encryption.Decrypt;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import com.eurodyn.qlack.util.data.filter.ReplyFilter;
import com.eurodyn.qlack.util.data.filter.ReplyPageableFilter;
import com.eurodyn.qlack.util.querydsl.EmptyPredicateCheck;
import com.github.slugify.Slugify;
import com.querydsl.core.types.Predicate;
import esthesis.platform.server.config.AppConstants.Cryptography.KeyType;
import esthesis.platform.server.dto.TunnelServerDTO;
import esthesis.platform.server.model.TunnelServer;
import esthesis.platform.server.service.SecurityService;
import esthesis.platform.server.service.TunnelServerService;
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

@RestController
@Validated
@RequestMapping("/ts")
public class TunnelServerResource {

  private final TunnelServerService tunnelServerService;
  private final SecurityService securityService;

  @GetMapping
  @EmptyPredicateCheck
  @ReplyPageableFilter("-certificate,-privateKey,-publicKey")
  public Page<TunnelServerDTO> findAll(@QuerydslPredicate(root = TunnelServer.class) Predicate predicate, Pageable pageable) {
    return tunnelServerService.findAll(predicate, pageable);
  }

  @GetMapping(value = "{id}")
  @ReplyFilter("-certificate,-privateKey,-publicKey")
  public TunnelServerDTO get(@PathVariable long id) {
    return tunnelServerService.findById(id);
  }

  public TunnelServerResource(TunnelServerService settingsTunnelService,
      SecurityService securityService) {
    this.tunnelServerService = settingsTunnelService;
    this.securityService = securityService;
  }

  @GetMapping(value = "{id}/backup")
  @ExceptionWrapper(wrapper = QExceptionWrapper.class,
      logMessage = "Could not create backup for Tunnel server.")
  public ResponseEntity backup(@PathVariable long id) throws IOException {
    final TunnelServerDTO tunnelServerDTO = tunnelServerService.findById(id);
    return ResponseEntity
        .ok()
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=" + new Slugify().slugify(tunnelServerDTO.getName()) + ".backup")
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(tunnelServerService.backup(id));
  }

  @PostMapping(value = "/restore")
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not restore Tunnel server.")
  public ResponseEntity restore(@NotNull @RequestParam("backup") MultipartFile backup) throws IOException {
    tunnelServerService.restore(IOUtils.toString(backup.getInputStream(), StandardCharsets.UTF_8));

    return ResponseEntity.ok().build();
  }

  @GetMapping(value = "{id}/download/{keyType}")
  @ExceptionWrapper(wrapper = QExceptionWrapper.class,
      logMessage = "Could not fetch security information for Tunnel server.")
  public ResponseEntity download(@PathVariable long id, @PathVariable int keyType) {
    final TunnelServerDTO tunnelServerDTO = tunnelServerService.findById(id);

    String filename = new Slugify().slugify(tunnelServerDTO.getName());
    String body = "";
    switch (keyType) {
      case KeyType.CERTIFICATE:
        filename += ".crt";
        body = tunnelServerDTO.getCertificate();
        break;
      case KeyType.PUBLIC_KEY:
        filename += ".pem";
        body = tunnelServerDTO.getPublicKey();
        break;
      case KeyType.PRIVATE_KEY:
        filename += ".key";
        body = securityService.decrypt(tunnelServerDTO.getPrivateKey());
        break;
    }

    return ResponseEntity
        .ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(body);
  }

  @DeleteMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not delete tunnel server.")
  public void delete(@PathVariable long id) {
    tunnelServerService.deleteById(id);
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not save tunnel server.")
  public TunnelServerDTO save(@Valid @RequestBody TunnelServerDTO tunnelServerDTO) throws Exception {
    return tunnelServerService.save(tunnelServerDTO);
  }

}
