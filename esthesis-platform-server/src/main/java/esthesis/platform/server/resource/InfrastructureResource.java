package esthesis.platform.server.resource;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import esthesis.platform.server.dto.InfrastructureReportDTO;
import esthesis.platform.server.service.InfrastructureService;
import esthesis.platform.server.service.MQTTService;
import esthesis.platform.server.service.ZookeeperService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Validated
@RestController
@RequestMapping("/infrastructure")
public class InfrastructureResource {

  private final InfrastructureService infrastructureService;
  private final ZookeeperService zookeeperService;
  private final MQTTService mqttService;

  public InfrastructureResource(InfrastructureService infrastructureService,
      ZookeeperService zookeeperService, MQTTService mqttService) {
    this.infrastructureService = infrastructureService;
    this.zookeeperService = zookeeperService;
    this.mqttService = mqttService;
  }

  @GetMapping
  public InfrastructureReportDTO get() {
    return infrastructureService.getReport();
  }

  @DeleteMapping(value = {"/rl/{resourceType}", "/rl/{resourceType}/{resourceId}"})
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not release leadership.")
  public ResponseEntity relenquishLeadership(@PathVariable final String resourceType,
      @PathVariable final Optional<Long> resourceId) throws Exception {
    switch (resourceType) {
      case "mqtt":
        if (resourceId.isPresent()) {
//          mqttService.releaseLeadership(resourceId.get());
        } else {
          return ResponseEntity.badRequest().build();
        }
        break;
      case "zookeeper":
//        zookeeperService.releaseLeadership();
        break;
    }

    return ResponseEntity.ok().build();
  }

}
