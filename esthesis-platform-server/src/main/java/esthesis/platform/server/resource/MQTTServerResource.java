package esthesis.platform.server.resource;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import com.eurodyn.qlack.util.querydsl.EmptyPredicateCheck;
import com.querydsl.core.types.Predicate;
import esthesis.platform.server.dto.MQTTServerDTO;
import esthesis.platform.server.model.MqttServer;
import esthesis.platform.server.service.MQTTService;
import javax.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mqtt-server")
@Validated
public class MQTTServerResource {

  private final MQTTService mqttServerService;

  public MQTTServerResource(MQTTService mqttServerService) {
    this.mqttServerService = mqttServerService;
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not retrieve MQTT servers list.")
  @EmptyPredicateCheck
  public Page<MQTTServerDTO> findAll(
    @QuerydslPredicate(root = MqttServer.class) Predicate predicate, Pageable pageable) {
    return mqttServerService.findAll(predicate, pageable);
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not save MQTT server.")
  public MQTTServerDTO save(@Valid @RequestBody MQTTServerDTO mqttServerDTO) {
    mqttServerDTO = mqttServerService.save(mqttServerDTO);
    return mqttServerDTO;
  }

  @GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not fetch MQTT server.")
  public MQTTServerDTO get(@PathVariable long id) {
    return mqttServerService.findById(id);
  }

  @DeleteMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not delete tag.")
  public void delete(@PathVariable long id) {
    mqttServerService.deleteById(id);
  }
}
