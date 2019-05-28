package esthesis.platform.server.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.text.MessageFormat;

/**
 * A proxy implementation to the {@link EsthesisAuditService} class to allow resolving {@link JWTService} parameters before
 * calling an async method for auditing (i.e. async method is executed on a different thread therefore JWTService does
 * not have a reference to the credentials of the user of the original thread).
 */
@Service
@Transactional
@Validated
public class EsthesisAuditServiceProxy {

  private final JWTService jwtService;
  private final EsthesisAuditService auditService;

  public EsthesisAuditServiceProxy(JWTService jwtService, EsthesisAuditService auditService) {
    this.jwtService = jwtService;
    this.auditService = auditService;
  }

  /**
   * Generic helper to audit events with info level.
   */
  public void info(String event, String message, Object... arguments) {
    info(event, MessageFormat.format(message, arguments));
  }

  /**
   * Generic helper to audit events with info level.
   */
  public void info(String event, String description) {
//    auditService.info(event, description, jwtService.getUserId());
  }

  /**
   * Generic helper to audit events with warning level.
   */
  public void warning(String event, String description) {
//    auditService.warning(event, description, jwtService.getUserId());
  }

  public void warning(String event, String message, Object... arguments) {
    warning(event, MessageFormat.format(message, arguments));
  }

  /**
   * Generic helper to audit events with error level.
   */
  public void error(String event, String description) {
//    auditService.error(event, description, jwtService.getUserId());
  }

  public void error(String event, String message, Object... arguments) {
    error(event, MessageFormat.format(message, arguments));
  }

}
