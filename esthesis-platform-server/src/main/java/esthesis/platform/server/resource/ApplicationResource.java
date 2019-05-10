package esthesis.platform.server.resource;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.common.util.KeyValue;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import com.eurodyn.qlack.util.data.filter.ReplyFilter;
import com.eurodyn.qlack.util.data.filter.ReplyPageableFilter;
import com.eurodyn.qlack.util.querydsl.EmptyPredicateCheck;
import com.querydsl.core.types.Predicate;
import esthesis.platform.server.config.AppConstants.Audit;
import esthesis.platform.server.dto.ApplicationDTO;
import esthesis.platform.server.model.Application;
import esthesis.platform.server.service.ApplicationService;
import esthesis.platform.server.service.AuditServiceProxy;
import javax.validation.Valid;
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

import java.util.Set;

@RestController
@Validated
@RequestMapping("/applications")
public class ApplicationResource {

  private final ApplicationService applicationService;
  private final AuditServiceProxy auditServiceProxy;

  public ApplicationResource(ApplicationService applicationService,
      AuditServiceProxy auditServiceProxy) {
    this.applicationService = applicationService;
    this.auditServiceProxy = auditServiceProxy;
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/status")
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not get application status.")
  public Set<KeyValue> getStatus() {
    return applicationService.getStatuses();
  }

  /**
   * Returns the profile of the current application.
   *
   * @return Returns the profile of the current application
   */
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class,
      logMessage = "There was a problem retrieving application data.")
  @ReplyPageableFilter("name,token,status,createdOn,id")
  @EmptyPredicateCheck
  public Page<ApplicationDTO> findAll(@QuerydslPredicate(root = Application.class) Predicate predicate,
      Pageable pageable) {
    return applicationService.findAll(predicate, pageable);
  }

  /**
   * Saves an application.
   *
   applicationService.save(applicationDTO);

   auditServiceProxy.info(Audit.EVENT_APPLICATION, "Application {0} saved.", applicationDTO.getName());

   return ResponseEntity.ok().build();
   }
   * @param applicationDTO The application to save
   */
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Save was unsuccessful.")
  public ResponseEntity save(@Valid @RequestBody ApplicationDTO applicationDTO) {
    applicationService.save(applicationDTO);

    auditServiceProxy.info(Audit.EVENT_APPLICATION, "Application {0} saved.", applicationDTO.getName());

    return ResponseEntity.ok().build();
  }

  @GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not fetch application.")
  @ReplyFilter("name,token,status,createdOn,id")
  public ApplicationDTO get(@PathVariable long id) {
    return applicationService.findById(id);
  }

  @DeleteMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not delete application.")
  public ResponseEntity delete(@PathVariable long id) {
    final ApplicationDTO applicationDTO = applicationService.deleteById(id);

    auditServiceProxy.info(Audit.EVENT_APPLICATION, "Application {0} deleted.", applicationDTO.getName());

    return ResponseEntity.ok().build();
  }
}
