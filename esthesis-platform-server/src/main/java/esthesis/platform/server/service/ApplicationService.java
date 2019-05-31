package esthesis.platform.server.service;

import static esthesis.platform.server.config.AppConstants.Audit.Level.CREATE;
import static esthesis.platform.server.config.AppConstants.Audit.Level.UPDATE;

import com.eurodyn.qlack.fuse.aaa.dto.UserDTO;
import com.eurodyn.qlack.fuse.aaa.service.UserService;
import com.eurodyn.qlack.fuse.audit.dto.AuditDTO;
import com.eurodyn.qlack.fuse.audit.service.AuditService;
import com.eurodyn.qlack.util.data.filter.JSONFilter;
import com.eurodyn.qlack.util.data.optional.ReturnOptional;
import esthesis.platform.server.config.AppConstants.Audit.Event;
import esthesis.platform.server.config.AppConstants.User.Status;
import esthesis.platform.server.dto.ApplicationDTO;
import esthesis.platform.server.mapper.ApplicationMapper;
import esthesis.platform.server.model.Application;
import esthesis.platform.server.repository.ApplicationRepository;
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
  private final ApplicationMapper applicationMapper;
  private final ApplicationRepository applicationRepository;

  public ApplicationService(AuditService auditService,
    UserService userService, ApplicationMapper applicationMapper,
    ApplicationRepository applicationRepository) {
    this.auditService = auditService;
    this.userService = userService;
    this.applicationMapper = applicationMapper;
    this.applicationRepository = applicationRepository;
  }

  @Override
  public ApplicationDTO save(ApplicationDTO applicationDTO) {
    // Filter read-only fields.
    applicationDTO = JSONFilter.filterNonEmpty(applicationDTO, "-userId");

    // Create a user for this application with an empty password, so this user can not login.
    String username = "app" + "_" + applicationDTO.getName();
    String userId = userService.createUser(new UserDTO()
      .setUsername(username)
      .setStatus(Status.APP_USER)
      .setStatus(Status.ACTIVE), null);

    // Save the application.
    applicationDTO.setUserId(userId);
    String auditLevel = applicationDTO.getId() == 0 ? CREATE : UPDATE;
    applicationDTO = super.save(applicationDTO);

    // Audit.
    String auditCorrelationId = UUID.randomUUID().toString();
    String auditMessage = "Application {0}" + (applicationDTO.getId() == 0 ? " created" : " "
      + "updated.");
    auditService.audit(new AuditDTO()
      .setLevel(auditLevel).setEvent(Event.APPLICATION)
      .setShortDescription(auditMessage, applicationDTO.getName())
      .setCorrelationId(auditCorrelationId));
    auditService.audit(new AuditDTO()
      .setLevel(auditLevel).setEvent(Event.USER)
      .setShortDescription("Created user {0} for application {1}.", username,
        applicationDTO.getName())
      .setCorrelationId(auditCorrelationId));

    return applicationDTO;
  }

  public ApplicationDTO findByToken(String token) {
    return applicationMapper.map(ReturnOptional.r(applicationRepository.findByTokenEquals(token)));
  }
}

