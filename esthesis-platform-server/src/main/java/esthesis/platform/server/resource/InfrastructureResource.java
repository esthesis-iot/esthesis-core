package esthesis.platform.server.resource;

import esthesis.platform.server.dto.InfrastructureReportDTO;
import esthesis.platform.server.service.InfrastructureService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/infrastructure")
public class InfrastructureResource {

  private final InfrastructureService infrastructureService;

  public InfrastructureResource(InfrastructureService infrastructureService) {
    this.infrastructureService = infrastructureService;
  }

  @GetMapping
  public InfrastructureReportDTO get() {
    return infrastructureService.getReport();
  }
}
