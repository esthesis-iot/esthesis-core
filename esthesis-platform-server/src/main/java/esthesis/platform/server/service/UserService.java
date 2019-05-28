package esthesis.platform.server.service;

import com.eurodyn.qlack.common.exception.QAuthenticationException;
import com.eurodyn.qlack.common.exception.QDoesNotExistException;
import com.eurodyn.qlack.fuse.aaa.dto.UserDTO;
import com.eurodyn.qlack.util.data.filter.JSONFilter;
import esthesis.platform.server.config.AppConstants.Audit;
import esthesis.platform.server.dto.jwt.JWTDTO;
import javax.validation.constraints.NotBlank;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.text.MessageFormat;
import java.util.UUID;

@Service("EsthesisUserService")
@Validated
@Transactional
public class UserService {

  private final JWTService jwtService;
  private final EsthesisAuditServiceProxy auditServiceProxy;
  private final EsthesisAuditService auditService;
  private final com.eurodyn.qlack.fuse.aaa.service.UserService qlackUserService;

  //  @Autowired
  public UserService(JWTService jwtService, EsthesisAuditServiceProxy auditServiceProxy,
    EsthesisAuditService auditService,
    com.eurodyn.qlack.fuse.aaa.service.UserService qlackUserService) {
    this.jwtService = jwtService;
    this.auditServiceProxy = auditServiceProxy;
    this.auditService = auditService;
    this.qlackUserService = qlackUserService;
  }

  private void verifyEmailIsUnique(String newEmail) {
    try {
      qlackUserService.getUserByName(newEmail);
    } catch (QDoesNotExistException e) {
      final String emailAlreadyExists = MessageFormat.format("Could not insert user. Email {0} "
          + "already exists.", newEmail);
      auditServiceProxy.warning(Audit.EVENT_PROFILE, emailAlreadyExists);
      throw new SecurityException(emailAlreadyExists);
    }
  }

  public UserDTO save(UserDTO userDTO) {
    // Exclude attributes the user should not change.
    userDTO = JSONFilter.filterNonEmpty(userDTO, "username,id,password,status");

    if (userDTO.getId().equals("0")) {
      userDTO.setId(UUID.randomUUID().toString());
      qlackUserService.createUser(userDTO, null);
    } else {
      qlackUserService.updateUser(userDTO, false, false);
    }

    return userDTO;
  }


  /**
   * Attempts to authenticate a user.
   *
   * @param email The email to authenticate with.
   * @param password The password to authenticate with.
   * @return Returns a JWT if authentication was successful, or null otherwise.
   */
  @Transactional(noRollbackFor = QAuthenticationException.class)
  public JWTDTO authenticate(@NotBlank String email, @NotBlank String password) {
    // The JWT to return if authentication was successful.
    String jwt;

    // Return an error if the user could not be authenticated.
    String userId = qlackUserService.canAuthenticate(email, password);
    if (StringUtils.isBlank(userId)) {
      throw new QAuthenticationException("User {0} could not authenticate.", email);
    }

    // Login the user and prepare a JWT.
    final UserDTO userDTO = qlackUserService.login(userId, null, true);

    jwt = jwtService.generateJwt(userDTO.getUsername(), userDTO.getId());

    return new JWTDTO().setJwt(jwt);
  }

  /**
   * Terminates a user session.
   *
   * @param userId The user Id to terminate.
   */
  @Async
  public void logout(String userId) {
    qlackUserService.logout(userId, null);
    //      auditService
    //          .info(AppConstants.Audit.EVENT_AUTHENTICATION, "User " + findById(userId).getEmail() + " logged out.", userId);
  }

}
