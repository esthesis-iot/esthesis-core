package esthesis.backend.resource;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import com.eurodyn.qlack.util.querydsl.EmptyPredicateCheck;
import com.querydsl.core.types.Predicate;
import esthesis.backend.dto.nifisinks.NiFiLoggerFactoryDTO;
import esthesis.backend.dto.nifisinks.NiFiProducerFactoryDTO;
import esthesis.backend.dto.nifisinks.NiFiReaderFactoryDTO;
import esthesis.backend.dto.nifisinks.NiFiSinkDTO;
import esthesis.backend.dto.nifisinks.NiFiWriterFactoryDTO;
import esthesis.backend.model.NiFiSink;
import esthesis.backend.service.NiFiSinkService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import java.util.List;

@RestController
@RequestMapping("/nifi-sinks")
@Validated
@RequiredArgsConstructor
public class NiFiSinkResource {

  private final NiFiSinkService niFiSinkService;

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class,
    logMessage = "Could not retrieve NiFi sinks list.")
  @EmptyPredicateCheck
  public Page<NiFiSinkDTO> findAll(@QuerydslPredicate(root = NiFiSink.class) Predicate predicate,
    Pageable pageable) {
    return niFiSinkService.findAll(predicate, pageable);
  }

  @GetMapping(path = "factories/readers", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class,
    logMessage = "Could not get available reader factories.")
  public List<NiFiReaderFactoryDTO> getReaderFactories() {
    return niFiSinkService.findAvailableNiFiReaderFactories();
  }

  @GetMapping(path = "factories/producers", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class,
    logMessage = "Could not get available producer factories.")
  public List<NiFiProducerFactoryDTO> getProducerFactories() {
    return niFiSinkService.findAvailableNiFiProducerFactories();
  }

  @GetMapping(path = "factories/writers", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class,
    logMessage = "Could not get available writer factories.")
  public List<NiFiWriterFactoryDTO> getWritersFactories() {
    return niFiSinkService.findAvailableNiFiWriterFactories();
  }

  @GetMapping(path = "factories/loggers", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class,
    logMessage = "Could not get available logger factories.")
  public List<NiFiLoggerFactoryDTO> getLoggerFactories() {
    return niFiSinkService.findAvailableNiFiLoggerFactories();
  }

  @GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not fetch NiFi sink.")
  public NiFiSinkDTO get(@PathVariable long id) {
    return niFiSinkService.findById(id);
  }

  @DeleteMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not delete NiFi sink.")
  public NiFiSinkDTO delete(@PathVariable long id) throws IOException {
    return niFiSinkService.deleteSink(id);
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not save NiFi sink.")
  public NiFiSinkDTO save(@Valid @RequestBody NiFiSinkDTO niFiSinkDTO)
  throws IOException, InterruptedException {
    return niFiSinkService.saveSink(niFiSinkDTO);
  }

  @GetMapping(path = "/synced", produces = MediaType.APPLICATION_JSON_VALUE)
  public boolean isSynced() {
    return niFiSinkService.isSynced();
  }


}
