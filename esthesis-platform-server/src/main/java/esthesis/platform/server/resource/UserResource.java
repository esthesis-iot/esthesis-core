package esthesis.platform.server.resource;

import com.eurodyn.qlack.common.exception.QAuthenticationException;
import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.common.util.KeyValue;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import com.eurodyn.qlack.util.data.filter.ReplyPageableFilter;
import com.eurodyn.qlack.util.querydsl.EmptyPredicateCheck;
import com.querydsl.core.types.Predicate;
import esthesis.platform.server.dto.LoginDTO;
import esthesis.platform.server.dto.UpdateUserProfileDTO;
import esthesis.platform.server.dto.UserDTO;
import esthesis.platform.server.dto.jwt.JWTDTO;
import esthesis.platform.server.model.User;
import esthesis.platform.server.service.AuditServiceProxy;
import esthesis.platform.server.service.JWTService;
import esthesis.platform.server.service.UserService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.logging.Logger;

@RestController
@RequestMapping("/users")
@Validated
public class UserResource {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(UserResource.class.getName());

  // Service references.
  private final JWTService jwtService;
  private final AuditServiceProxy auditServiceProxy;
  private final UserService userService;

  @Autowired
  public UserResource(JWTService jwtService,
      AuditServiceProxy auditServiceProxy,
      UserService userService) {
    this.jwtService = jwtService;
    this.auditServiceProxy = auditServiceProxy;
    this.userService = userService;
  }

  /**
   * Authenticates a user and returns a JWT if authentication was successful.
   *
   * @param loginDTO The email and password of the user to authenticate.
   * @return Returns the JWT.
   */
  @PostMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
  @Transactional(noRollbackFor = QAuthenticationException.class)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, ignore = {
      QAuthenticationException.class})
  public JWTDTO authenticate(@RequestBody LoginDTO loginDTO) {
    // Try to authenticate the user.
    return userService.authenticate(loginDTO.getEmail(), loginDTO.getPassword(), false);
  }


  /**
   * Returns the profile of the currently registered user.
   *
   * @return Returns the profile of the currently registered user.
   */
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class,
      logMessage = "There was a problem getting your user profile.")
  @ReplyPageableFilter("createdOn,email,fn,ln,id,status,userType")
  @EmptyPredicateCheck
  public Page<UserDTO> findAll(@QuerydslPredicate(root = User.class) Predicate predicate, Pageable pageable) {
    return userService.findAll(predicate, pageable);
  }

  /**
   * Saves an object.
   *
   * @param userDTO The object to save.
   */
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Save was unsuccessful.")
  public UserDTO save(@Valid @RequestBody UserDTO userDTO) {
    return userService.save(userDTO);
  }

  @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "There was a problem updating your user profile.")
  public ResponseEntity updateUserProfile(@RequestBody UpdateUserProfileDTO updateUserProfileDTO) {
    userService.updateUserProfile(updateUserProfileDTO);
    return ResponseEntity.ok().build();
  }

  /**
   * Logs a user out of the application, effectively terminating its session in AAA.
   *
   * @return Returns an HTTP OK or error.
   */
  @GetMapping(value = "logout")
  @ExceptionWrapper(wrapper = QExceptionWrapper.class,
      logMessage = "There was a problem logging you out.")
  public ResponseEntity logout() {
    userService.logout(jwtService.getUserId());
    return ResponseEntity.ok().build();
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/status")
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not get user status.")
  public Set<KeyValue> getStatus() {
    return userService.getStatus();
  }

}
