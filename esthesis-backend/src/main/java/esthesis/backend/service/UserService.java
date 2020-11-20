package esthesis.backend.service;

import com.eurodyn.qlack.common.exception.QAuthenticationException;
import com.eurodyn.qlack.common.exception.QDoesNotExistException;
import com.eurodyn.qlack.fuse.aaa.dto.UserDTO;
import com.eurodyn.qlack.fuse.audit.dto.AuditDTO;
import com.eurodyn.qlack.fuse.audit.service.AuditAsyncService;
import com.eurodyn.qlack.util.data.filter.JSONFilter;
import com.eurodyn.qlack.util.jwt.dto.JwtDTO;
import com.eurodyn.qlack.util.jwt.dto.JwtGenerateRequestDTO;
import com.eurodyn.qlack.util.jwt.service.JwtService;
import esthesis.backend.config.AppConstants.Audit.Event;
import esthesis.backend.config.AppConstants.Audit.Level;
import esthesis.backend.config.AppConstants.Jwt;
import javax.validation.constraints.NotBlank;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.UUID;

@Service("EsthesisUserService")
@Validated
@Transactional
public class UserService {

  private final JwtService jwtService;
  private final AuditAsyncService auditService;
  private final com.eurodyn.qlack.fuse.aaa.service.UserService qlackUserService;

  public UserService(JwtService jwtService,
    AuditAsyncService auditService,
    com.eurodyn.qlack.fuse.aaa.service.UserService qlackUserService
  ) {
    this.jwtService = jwtService;
    this.auditService = auditService;
    this.qlackUserService = qlackUserService;
  }

  private void verifyEmailIsUnique(String newEmail) {
    try {
      qlackUserService.getUserByName(newEmail);
    } catch (QDoesNotExistException e) {
      final String emailAlreadyExists = MessageFormat.format("Could not insert user. Email {0} "
        + "already exists.", newEmail);
      //      auditServiceProxy.warning(Audit.EVENT_PROFILE, emailAlreadyExists);
      throw new SecurityException(emailAlreadyExists);
    }
  }

  public UserDTO save(UserDTO userDTO) {
    // Exclude attributes the user should not change.
    userDTO = JSONFilter.filterNonEmpty(userDTO, "username,id,password,status");

    if (userDTO.getId().equals("0")) {
      userDTO.setId(UUID.randomUUID().toString());
      qlackUserService.createUser(userDTO, Optional.empty());
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
  public JwtDTO authenticate(@NotBlank String email, @NotBlank String password) {
    // Return an error if the user could not be authenticated.
    String userId = qlackUserService.canAuthenticate(email, password);
    if (StringUtils.isBlank(userId)) {
      auditService.audit(new AuditDTO()
        .setLevel(Level.SECURITY)
        .setEvent(Event.AUTHENTICATION)
        .setOpt1(email)
        .setShortDescription(MessageFormat.format("User {0} could not be authenticated.", email)));
      throw new QAuthenticationException("User {0} could not authenticate.", email);
    }

    // Login the user and prepare a JWT.
    final UserDTO userDTO = qlackUserService.login(userId, null, true);
    auditService.audit(Level.SECURITY, Event.AUTHENTICATION,
      "User {0} authenticated successfully.", email);
    final String jwt = jwtService.generateJwt(JwtGenerateRequestDTO.builder()
      .subject(userDTO.getId())
      .claim(Jwt.CLAIM_EMAIL, userDTO.getUsername())
      .build());

    return JwtDTO.builder().jwt(jwt).build();
  }

  /**
   * Terminates a user session.
   *
   * @param userId The user Id to terminate.
   */
  @Async
  public void logout(String userId) {
    qlackUserService.logout(userId, null);
  }

}
