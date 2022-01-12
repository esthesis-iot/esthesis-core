package esthesis.platform.backend.server.resource;

import com.eurodyn.qlack.common.exception.QAuthenticationException;
import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.fuse.aaa.dto.UserDTO;
import com.eurodyn.qlack.fuse.aaa.model.User;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import com.eurodyn.qlack.util.data.filter.ReplyFilter;
import com.eurodyn.qlack.util.data.filter.ReplyPageableFilter;
import com.eurodyn.qlack.util.jwt.dto.JwtDTO;
import com.eurodyn.qlack.util.querydsl.EmptyPredicateCheck;
import com.querydsl.core.types.Predicate;
import esthesis.platform.backend.server.dto.LoginDTO;
import esthesis.platform.backend.server.service.UserService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/users")
public class UserResource {

  // Service references.
  private final com.eurodyn.qlack.fuse.aaa.service.UserService qlackUserService;
  @Qualifier("EsthesisUserService")
  private final UserService userService;

  @Autowired
  public UserResource(com.eurodyn.qlack.fuse.aaa.service.UserService qlackUserService,
    UserService userService) {
    this.qlackUserService = qlackUserService;
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
  public JwtDTO authenticate(@RequestBody LoginDTO loginDTO) {
    // Try to authenticate the user.
    return userService.authenticate(loginDTO.getEmail(), loginDTO.getPassword());
  }

  /**
   * Returns the profile of the currently registered user.
   *
   * @return Returns the profile of the currently registered user.
   */
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class,
    logMessage = "Could not fetch list of users.")
  @ReplyPageableFilter("id,status,username")
  @EmptyPredicateCheck
  public Page<UserDTO> findAll(@QuerydslPredicate(root = User.class) Predicate predicate,
    Pageable pageable) {
    return qlackUserService.findAll(predicate, pageable);
  }

  @GetMapping(path = "{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not fetch user.")
  @ReplyFilter("id,status,username")
  public UserDTO get(@PathVariable String userId) {
    return qlackUserService.getUserById(userId);
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

  /**
   * Logs a user out of the application, effectively terminating its session in AAA.
   *
   * @return Returns an HTTP OK or error.
   */
  @GetMapping(value = "logout")
  @ExceptionWrapper(wrapper = QExceptionWrapper.class,
    logMessage = "There was a problem logging you out.")
  public ResponseEntity logout() {
    userService
      .logout(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
    return ResponseEntity.ok().build();
  }

  @DeleteMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not delete user.")
  public void delete(@PathVariable String id) {
    qlackUserService.deleteUser(id);
  }
}
