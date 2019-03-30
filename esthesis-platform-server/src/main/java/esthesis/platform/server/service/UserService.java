package esthesis.platform.server.service;

import static java.text.MessageFormat.format;

import com.eurodyn.qlack.common.exception.QAuthenticationException;
import com.eurodyn.qlack.common.exception.QSecurityException;
import com.eurodyn.qlack.common.util.KeyValue;
import com.eurodyn.qlack.util.data.optional.ReturnOptional;
import com.google.common.collect.Sets;
import esthesis.platform.server.config.AppConstants;
import esthesis.platform.server.config.AppConstants.Audit;
import esthesis.platform.server.dto.UpdateUserProfileDTO;
import esthesis.platform.server.dto.UserDTO;
import esthesis.platform.server.dto.jwt.JWTDTO;
import esthesis.platform.server.mapper.UserMapper;
import esthesis.platform.server.model.User;
import esthesis.platform.server.repository.UserRepository;
import javax.validation.constraints.NotBlank;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Set;

@Service
@Validated
@Transactional
public class UserService extends BaseService<UserDTO, User> {

  private final UserRepository userRepository;
  private final JWTService jwtService;
  private final AuditServiceProxy auditServiceProxy;
  private final AuditService auditService;
  private final UserMapper userMapper;

  @Autowired
  public UserService(UserRepository userRepository, JWTService jwtService, AuditServiceProxy auditServiceProxy,
      AuditService auditService, UserMapper userMapper) {
    this.userRepository = userRepository;
    this.jwtService = jwtService;
    this.auditServiceProxy = auditServiceProxy;
    this.auditService = auditService;
    this.userMapper = userMapper;
  }

  private String createPassword(String password, String salt) {
    return DigestUtils.md5Hex(salt + password);
  }

  private boolean verifyPassword(User user, String password, boolean isHashed) {
    String checkPassword;
    if (!isHashed) {
      checkPassword = DigestUtils.md5Hex(user.getSalt() + password);
    } else {
      checkPassword = password;
    }
    return checkPassword.equals(user.getPassword());
  }

  private User findUserByEmail(String email) {
    return ReturnOptional.r(userRepository.findUserByEmail(email), email);
  }

  private void verifyEmailIsUnique(String newEmail) {
    if (userRepository.findUserByEmail(newEmail).isPresent()) {
      final String emailAlreadyExists = format("Could not insert user. Email {0} already exists.",
          newEmail);
      auditServiceProxy.warning(Audit.EVENT_PROFILE, emailAlreadyExists);
      throw new QSecurityException(emailAlreadyExists);
    }
  }

  private void updateEmail(UpdateUserProfileDTO updateUserProfileDTO, User user) {
    // Before allowing the email to be changed check that the current password is valid.
    if (!verifyPassword(user, updateUserProfileDTO.getOldPassword(), false)) {
      auditServiceProxy.error(Audit.EVENT_PROFILE,
          "Could not change email address due to wrong password.");
      throw new QSecurityException(
          "To change your email address you need to enter your current password.");
    }
    verifyEmailIsUnique(updateUserProfileDTO.getNewEmail());

    user.setEmail(updateUserProfileDTO.getNewEmail());

    // Audit.
    auditServiceProxy.info(Audit.EVENT_PROFILE, "Email address was changed.");
  }

  private void updatePassword(UpdateUserProfileDTO updateUserProfileDTO, User user) {
    if (StringUtils.isNotBlank(updateUserProfileDTO.getOldPassword()) &&
        updateUserProfileDTO.getNewPassword1().equals(updateUserProfileDTO.getNewPassword2()) &&
        verifyPassword(user, updateUserProfileDTO.getOldPassword(), true)) {
      user.setPassword(createPassword(updateUserProfileDTO.getNewPassword1(), user.getSalt()));
      auditServiceProxy.info(Audit.EVENT_PROFILE, "Changed password.");
    } else {
      auditServiceProxy.error(Audit.EVENT_PROFILE, "Could not change password.");
      throw new QSecurityException("Could not change password for user {0}.", user.getId());
    }
  }

  public UserDTO save(UserDTO user, boolean createPassword) {
    if (createPassword) {
      user.setPassword(createPassword(user.getPassword(), user.getSalt()));
    }
    verifyEmailIsUnique(user.getEmail());

    return userMapper.map(userRepository.save(userMapper.map(user)));
  }

  /**
   * Attempts to authenticate a user.
   *
   * @param email The email to authenticate with.
   * @param password The password to authenticate with.
   * @param skipPasswordCheck Allows to skip password check in order to automatically authenticate users that just
   * signed up.
   * @return Returns a JWT if authentication was successful, or null otherwise.
   */
  @Transactional(noRollbackFor = QAuthenticationException.class)
  public JWTDTO authenticate(@NotBlank String email, @NotBlank String password,
      boolean skipPasswordCheck) {
    // The JWT to return if authentication was successful.
    String jwt;

    // Find the user, if it exists.
    //TODO check user status
    User user = findUserByEmail(email);
    if (user != null) {
      // Try to login the user.
      if (!skipPasswordCheck && !verifyPassword(user, password, false)) {
        auditServiceProxy
            .warning(Audit.EVENT_AUTHENTICATION,
                format("User {0} could not  login, wrong password.", user.getId()));
        throw new QAuthenticationException("User {0} could not login.", user.getId());
      } else {

        // Generate JWT.
        jwt = jwtService.generateJwt(user.getEmail(), user.getId());

        // Auditing.
        auditService
            .info(AppConstants.Audit.EVENT_AUTHENTICATION, "User " + user.getEmail() + " logged in.", user.getId());
      }
    } else {
      throw new QAuthenticationException("User {0} could not be found.", email);
    }

    return new JWTDTO().setJwt(jwt);
  }

  public void updateUserProfile(UpdateUserProfileDTO updateUserProfileDTO) {
    // Get user.
    User user = ReturnOptional.r(userRepository.findById(updateUserProfileDTO.getId()));

    // Update attributes.
    user.setFn(updateUserProfileDTO.getFn());
    user.setLn(updateUserProfileDTO.getLn());

    // Update password - if required.
    if (StringUtils.isNotBlank(updateUserProfileDTO.getNewPassword1())) {
      updatePassword(updateUserProfileDTO, user);
    }

    // Update email address.
    if (StringUtils.isNotBlank(updateUserProfileDTO.getNewEmail())) {
      updateEmail(updateUserProfileDTO, user);
    }

    userRepository.save(user);
  }

  /**
   * Terminates a user session.
   *
   * @param userId The user Id to terminate.
   */
  @Async
  public void logout(long userId) {
    auditService
        .info(AppConstants.Audit.EVENT_AUTHENTICATION, "User " + findById(userId).getEmail() + " logged out.", userId);
  }

  public Set<KeyValue> getStatus() {
    return Sets.newHashSet(
        KeyValue.builder().key(AppConstants.User.STATUS_DISABLED).value("Disabled").build(),
        KeyValue.builder().key(AppConstants.User.STATUS_ACTIVE).value("Active").build(),
        KeyValue.builder().key(AppConstants.User.STATUS_INACTIVE).value("Inactive").build());
  }

}