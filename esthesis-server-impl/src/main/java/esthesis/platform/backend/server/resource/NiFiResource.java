package esthesis.platform.backend.server.resource;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import com.eurodyn.qlack.util.data.filter.ReplyPageableFilter;
import com.eurodyn.qlack.util.querydsl.EmptyPredicateCheck;
import com.querydsl.core.types.Predicate;
import esthesis.platform.backend.server.dto.NiFiDTO;
import esthesis.platform.backend.server.dto.WfVersionDTO;
import esthesis.platform.backend.server.model.NiFi;
import esthesis.platform.backend.server.service.NiFiService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/infrastructure/nifi")
@RequiredArgsConstructor
public class NiFiResource {

  private final NiFiService nifiService;

  /**
   * Returns the profile of the current application.
   *
   * @return Returns the profile of the current application
   */
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class,
    logMessage = "There was a problem retrieving application data.")
  @ReplyPageableFilter("-createdOn,-createdBy,-modifiedOn,-modifiedBy")
  @EmptyPredicateCheck
  public Page<NiFiDTO> findAll(
    @QuerydslPredicate(root = NiFi.class) Predicate predicate, Pageable pageable) {
    return nifiService.findAll(predicate, pageable);
  }

  /**
   * Saves an application.
   *
   * @param nifiDTO The NiFi object to save
   */
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Save was unsuccessful.")
  public ResponseEntity save(@Valid @RequestBody NiFiDTO nifiDTO) {
    nifiService.save(nifiDTO);
    return ResponseEntity.ok().build();
  }

  @GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not fetch NiFi object.")
  //@ReplyFilter("name,token,state,createdOn,id")
  public NiFiDTO get(@PathVariable long id) {
    return nifiService.findById(id);
  }

  @DeleteMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not delete NiFi object.")
  public ResponseEntity delete(@PathVariable long id) {
    nifiService.deleteById(id);

    return ResponseEntity.ok().build();
  }

  @GetMapping(path = "/latest-wf-version", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not fetch latest workflow version.")
  public WfVersionDTO get() {
    return new WfVersionDTO()
      .setVersion(nifiService.getLatestWFVersion());
  }

  @GetMapping(path = "/active", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not fetch NiFi object.")
  public NiFiDTO getActive() {
    return nifiService.getActiveNiFi();
  }
}
