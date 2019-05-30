package esthesis.platform.server.service;

import static esthesis.platform.server.config.AppConstants.Audit.Level.CREATE;
import static esthesis.platform.server.config.AppConstants.Audit.Level.UPDATE;

import com.eurodyn.qlack.fuse.aaa.dto.UserDTO;
import com.eurodyn.qlack.fuse.aaa.service.UserService;
import com.eurodyn.qlack.fuse.audit.dto.AuditDTO;
import com.eurodyn.qlack.fuse.audit.service.AuditService;
import esthesis.platform.server.config.AppConstants.Audit.Event;
import esthesis.platform.server.config.AppConstants.User.Status;
import esthesis.platform.server.dto.ApplicationDTO;
import esthesis.platform.server.model.Application;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Service
@Transactional
@Validated
public class ApplicationService extends BaseService<ApplicationDTO, Application> {

  private final AuditService auditService;
  private final UserService userService;

  public ApplicationService(AuditService auditService,
    UserService userService) {
    this.auditService = auditService;
    this.userService = userService;
  }

  @Override
  public ApplicationDTO save(ApplicationDTO applicationDTO) {
    // Save the application.
    applicationDTO = super.save(applicationDTO);

    // Create a user for this application with an empty password, so this user can not login.
    String username = "app" + "_" + applicationDTO.getName();
    userService.createUser(new UserDTO()
      .setUsername(username)
      .setStatus(Status.APP_USER)
      .setStatus(Status.ACTIVE), null);

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

    return applicationDTO;
  }
}

