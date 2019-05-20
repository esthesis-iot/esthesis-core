package esthesis.platform.server.resource;

import static esthesis.platform.server.events.LocalEvent.LOCAL_EVENT_TYPE.CONFIGURATION_ZOOKEEPER;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import com.eurodyn.qlack.util.querydsl.EmptyPredicateCheck;
import com.querydsl.core.types.Predicate;
import esthesis.platform.server.dto.ZookeeperServerDTO;
import esthesis.platform.server.events.LocalEvent;
import esthesis.platform.server.model.ZookeeperServer;
import esthesis.platform.server.service.ZookeeperService;
import javax.validation.Valid;
import org.springframework.context.ApplicationEventPublisher;
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

import java.io.IOException;

@RestController
@RequestMapping("/zookeeper-server")
@Validated
public class ZookeeperServerResource {
  private final ZookeeperService zookeeperServerService;
  private final ApplicationEventPublisher applicationEventPublisher;

  public ZookeeperServerResource(ZookeeperService zookeeperServerService,
    ApplicationEventPublisher applicationEventPublisher) {
    this.zookeeperServerService = zookeeperServerService;
    this.applicationEventPublisher = applicationEventPublisher;
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not retrieve Zookeeper servers list.")
  @EmptyPredicateCheck
  public Page<ZookeeperServerDTO> findAll(@QuerydslPredicate(root = ZookeeperServer.class) Predicate predicate, Pageable pageable) {
    return zookeeperServerService.findAll(predicate, pageable);
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not save Zookeeper server.")
  public ZookeeperServerDTO save(@Valid @RequestBody ZookeeperServerDTO zookeeperServerDTO) throws IOException {
    zookeeperServerDTO = zookeeperServerService.save(zookeeperServerDTO);
    // Emit an event about this configuration change.
    applicationEventPublisher.publishEvent(new LocalEvent(CONFIGURATION_ZOOKEEPER));

    return zookeeperServerDTO;
  }

  @GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not fetch Zookeeper server.")
  public ZookeeperServerDTO get(@PathVariable long id) {
    return zookeeperServerService.findById(id);
  }

  @DeleteMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not delete tag.")
  public void delete(@PathVariable long id) {
    applicationEventPublisher.publishEvent(new LocalEvent(CONFIGURATION_ZOOKEEPER));
    zookeeperServerService.deleteById(id);
  }
}
