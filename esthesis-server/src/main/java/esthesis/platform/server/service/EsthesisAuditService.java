package esthesis.platform.server.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/**
 * A helper service to allow others modules to write to the audit log by automatically providing default values such as
 * the user id.
 */
@Service
@Transactional
@Validated
public class EsthesisAuditService {

//  /**
//   * Creates a new audit entry.
//   *
//   * @param level The log level of the entry.
//   * @param event The type of the event to be audited.
//   * @param description The description about the event to be audited.
//   * @param userId The user id executing the action to be audited.
//   */
//  private void audit(String level, String event,
//      String description, long userId) {
//    final Audit audit = new Audit()
//        .setLevel(level)
//        .setEvent(event)
//        .setDescription(description);
//
//    if (userId != User.SYSTEM_USER_ID) {
//      audit.setUser(userRepository.findById(userId).orElse(null));
//    }
//
//    auditRepository.save(audit);
//  }
//
//  /**
//   * Generic helper to audit events.
//   */
//  @Async
//  public void info(String event, String description, long userId) {
//    audit(LOG_LEVEL_INFO, event, description, userId);
//  }
//
//  /**
//   * Generic helper to audit events.
//   */
//  @Async
//  public void warning(String event, String description, long userId) {
//    audit(LOG_LEVEL_WARNING, event, description, userId);
//  }
//
//  /**
//   * Generic helper to audit events.
//   */
//  @Async
//  public void error(String event, String description, long userId) {
//    audit(LOG_LEVEL_ERROR, event, description, userId);
//  }
//
//  /**
//   * Returns all available audit logs.
//   *
//   * @param pageable A Spring {@link Pageable} object encapsulating results paging.
//   * @param predicate A QueryDSL {@link Predicate} allowing filtering of the audit events.
//   */
//  public Page<AuditDTO> getAudits(Predicate predicate, Pageable pageable) {
//    return auditMapper.map(auditRepository.findAll(predicate, pageable));
//  }
//
//  /**
//   * Returns all event types in currently audited data.
//   */
//  public List<String> getEvents() {
//    return auditRepository.findDistinctEvents();
//  }
//
//  /**
//   * Returns all audit levels in currently audited data.
//   */
//  public List<String> getAuditLevels() {
//    return auditRepository.findDistinctLevels();
//  }
//
//  /**
//   * Returns all audit levels in currently audited data.
//   */
//  public List<UserDTO> getUsers() {
//    return userMaper.map(auditRepository.findDistinctUsers());
//  }
//
//  public Page<AuditDTO> findAll(Predicate predicate, Pageable pageable) {
//    return auditMapper.map(auditRepository.findAll(predicate, pageable));
//  }
}
