package esthesis.platform.server.resource;

import static esthesis.platform.server.config.AppConstants.Audit.Level.CREATE;
import static esthesis.platform.server.config.AppConstants.Audit.Level.UPDATE;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.fuse.aaa.service.UserService;
import com.eurodyn.qlack.fuse.audit.dto.AuditDTO;
import com.eurodyn.qlack.fuse.audit.service.AuditService;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import com.eurodyn.qlack.util.data.filter.ReplyFilter;
import com.eurodyn.qlack.util.data.filter.ReplyPageableFilter;
import com.eurodyn.qlack.util.querydsl.EmptyPredicateCheck;
import com.querydsl.core.types.Predicate;
import esthesis.platform.server.config.AppConstants.Audit.Event;
import esthesis.platform.server.dto.ApplicationDTO;
import esthesis.platform.server.model.Application;
import esthesis.platform.server.service.ApplicationService;
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

import java.util.UUID;

@RestController
@Validated
@RequestMapping("/applications")
public class ApplicationResource {

  private final ApplicationService applicationService;
  private final AuditService auditService;
  private final UserService userService;

  public ApplicationResource(ApplicationService applicationService,
    AuditService auditService, UserService userService) {
    this.applicationService = applicationService;
    this.auditService = auditService;
    this.userService = userService;
  }

  /**
   * Returns the profile of the current application.
   *
   * @return Returns the profile of the current application
   */
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class,
    logMessage = "There was a problem retrieving application data.")
  @ReplyPageableFilter("name,token,state,createdOn,id")
  @EmptyPredicateCheck
  public Page<ApplicationDTO> findAll(
    @QuerydslPredicate(root = Application.class) Predicate predicate,
    Pageable pageable) {
    return applicationService.findAll(predicate, pageable);
  }

  /**
   * Saves an application.
   *
   * @param applicationDTO The application to save
   */
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Save was unsuccessful.")
  public ResponseEntity save(@Valid @RequestBody ApplicationDTO applicationDTO) {
    // Save the application.
    applicationService.save(applicationDTO);

    // Create a user for this application.
//    UserDTO userDTO = new UserDTO().set
//    userService.createUser()

    // Audit.
    String auditCorrelationId = UUID.randomUUID().toString();
    String auditLevel = applicationDTO.getId() == 0 ? CREATE : UPDATE;
    String auditMessage = "Application {0}" + (applicationDTO.getId() == 0 ? " created" : " "
      + "updated.");
    auditService.audit(new AuditDTO()
      .setLevel(auditLevel).setEvent(Event.APPLICATION)
      .setShortDescription(auditMessage, applicationDTO.getName())
      .setCorrelationId(auditCorrelationId));
    auditService.audit(new AuditDTO()
      .setLevel(auditLevel).setEvent(Event.USER)
      .setShortDescription("Created user {0} for application {1}.")
      .setCorrelationId(auditCorrelationId));

    return ResponseEntity.ok().build();
  }

  @GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not fetch application.")
  @ReplyFilter("name,token,state,createdOn,id")
  public ApplicationDTO get(@PathVariable long id) {
    return applicationService.findById(id);
  }

  @DeleteMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not delete application.")
  public ResponseEntity delete(@PathVariable long id) {
    final ApplicationDTO applicationDTO = applicationService.deleteById(id);

    return ResponseEntity.ok().build();
  }
}
