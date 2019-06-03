package esthesis.platform.server.resource.dt;

import esthesis.platform.server.service.DTService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/dt")
public class DTDeviceResource {
  private final DTService dtService;

  public DTDeviceResource(DTService dtService) {
    this.dtService = dtService;
  }

  @GetMapping
  public ResponseEntity getDevices() {
    System.out.println(
    SecurityContextHolder.getContext().getAuthentication().getCredentials()
    );
    return ResponseEntity.ok().build();
  }

  @GetMapping(path = "test")
  public ResponseEntity getDevices2() {
    return ResponseEntity.ok().build();
  }
}
