package esthesis.platform.server.resource;

import static esthesis.platform.server.events.LocalEvent.LOCAL_EVENT_TYPE.CONFIGURATION_DATASINK;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import com.eurodyn.qlack.util.querydsl.EmptyPredicateCheck;
import com.querydsl.core.types.Predicate;
import esthesis.platform.server.dto.DataSinkDTO;
import esthesis.platform.server.dto.DataSinkFactoryDTO;
import esthesis.platform.server.events.LocalEvent;
import esthesis.platform.server.model.DataSink;
import esthesis.platform.server.service.DataSinkService;
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

import java.util.List;

@RestController
@RequestMapping("/data-sinks")
@Validated
public class DataSinksResource {

  private final DataSinkService dataSinkService;
  private final ApplicationEventPublisher applicationEventPublisher;

  public DataSinksResource(DataSinkService dataSinkService,
    ApplicationEventPublisher applicationEventPublisher) {
    this.dataSinkService = dataSinkService;
    this.applicationEventPublisher = applicationEventPublisher;
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class,
    logMessage = "Could not retrieve data sinks list.")
  @EmptyPredicateCheck
  public Page<DataSinkDTO> findAll(@QuerydslPredicate(root = DataSink.class) Predicate predicate,
    Pageable pageable) {
    return dataSinkService.findAll(predicate, pageable);
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class,
    logMessage = "Could not save data sink.")
  public DataSinkDTO save(@Valid @RequestBody DataSinkDTO dataSinkDTO) {
    dataSinkDTO = dataSinkService.save(dataSinkDTO);
    // Emit an event about this configuration change.
    applicationEventPublisher.publishEvent(new LocalEvent(CONFIGURATION_DATASINK));

    return dataSinkDTO;
  }

  @GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not fetch data sink.")
  public DataSinkDTO get(@PathVariable long id) {
    return dataSinkService.findById(id);
  }

  @GetMapping(path = "factories", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class,
    logMessage = "Could not get available data sink factories.")
  public List<DataSinkFactoryDTO> getFactories() {
    return dataSinkService.findAvailableDataSinkFactories();
  }

  @DeleteMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not delete data sink.")
  public void delete(@PathVariable long id) {
    dataSinkService.deleteById(id);

    // Emit an event about this configuration change.
    applicationEventPublisher.publishEvent(new LocalEvent(CONFIGURATION_DATASINK));
  }
}
