package esthesis.backend.service;

import static esthesis.backend.config.AppConstants.Audit.Level.CREATE;
import static esthesis.backend.config.AppConstants.Audit.Level.UPDATE;

import com.eurodyn.qlack.fuse.aaa.dto.UserDTO;
import com.eurodyn.qlack.fuse.aaa.service.UserService;
import com.eurodyn.qlack.fuse.audit.dto.AuditDTO;
import com.eurodyn.qlack.fuse.audit.service.AuditService;
import com.eurodyn.qlack.util.data.filter.JSONFilter;
import com.eurodyn.qlack.util.data.optional.ReturnOptional;
import esthesis.backend.repository.ApplicationRepository;
import esthesis.backend.config.AppConstants.Audit.Event;
import esthesis.backend.config.AppConstants.User.Status;
import esthesis.backend.dto.ApplicationDTO;
import esthesis.backend.mapper.ApplicationMapper;
import esthesis.backend.model.Application;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@Validated
public class ApplicationService extends BaseService<ApplicationDTO, Application> {

  private final AuditService auditService;
  private final UserService userService;
  private final ApplicationMapper applicationMapper;
  private final ApplicationRepository applicationRepository;
  public static final String APPLICATION_PREFIX = "Application ";

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

    String auditLevel;
    String auditCorrelationId;
    if (applicationDTO.getId() == null) {
      auditLevel = CREATE;
      auditCorrelationId = UUID.randomUUID().toString();
      String username = APPLICATION_PREFIX + applicationDTO.getName();
      String userId = userService.createUser(new UserDTO()
        .setUsername(username)
        .setStatus(Status.APP_USER)
        .setStatus(Status.ACTIVE), Optional.empty());
      applicationDTO.setUserId(userId);
      auditService.audit(new AuditDTO()
        .setLevel(auditLevel).setEvent(Event.USER)
        .setShortDescription("Created user {0} for application {1}.", username,
          applicationDTO.getName())
        .setCorrelationId(auditCorrelationId));
    } else {
      auditLevel = UPDATE;
      auditCorrelationId = null;
    }

    applicationDTO = super.save(applicationDTO);

    // Audit.
    String auditMessage = "Application {0}" + (applicationDTO.getId() == 0 ? " created" : " "
      + "updated.");
    auditService.audit(new AuditDTO()
      .setLevel(auditLevel).setEvent(Event.APPLICATION)
      .setShortDescription(auditMessage, applicationDTO.getName())
      .setCorrelationId(auditCorrelationId));

    return applicationDTO;
  }

  public ApplicationDTO findByToken(String token) {
    return applicationMapper.map(ReturnOptional.r(applicationRepository.findByTokenEquals(token)));
  }

  @Override
  public ApplicationDTO deleteById(long id) {
    // Delete the user for this application.
    userService.deleteUser(findById(id).getUserId());

    // Delete the application.
    return super.deleteById(id);
  }
}

