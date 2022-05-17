package esthesis.platform.server.resource;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.common.util.KeyValue;
import com.eurodyn.qlack.fuse.audit.dto.AuditDTO;
import com.eurodyn.qlack.fuse.audit.model.Audit;
import com.eurodyn.qlack.fuse.audit.service.AuditService;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import com.eurodyn.qlack.util.data.filter.ReplyPageableFilter;
import com.eurodyn.qlack.util.querydsl.EmptyPredicateCheck;
import com.querydsl.core.types.Predicate;
import esthesis.platform.server.config.AppConstants;
import esthesis.platform.server.util.ReflectionUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/audit")
public class AuditResource {

  private final AuditService auditService;

  public AuditResource(AuditService auditService) {
    this.auditService = auditService;
  }

  /**
   * Fetches all audit events using specific paging parameters.
   *
   * @param pageable The paging parameters to filter results with.
   */
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not obtain audit reports.")
  @EmptyPredicateCheck
  @ReplyPageableFilter("createdOn,shortDescription,event,id,level")
  public Page<AuditDTO> findAll(@QuerydslPredicate(root = Audit.class) Predicate predicate,
    Pageable pageable) {
    return auditService.getAuditLogs(pageable, predicate);
  }


  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/events")
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not obtain events list.")
  public List<KeyValue<String, String>> getEvents() {
    return ReflectionUtils.getStaticFieldsValue(AppConstants.Audit.Event.class);
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/levels")
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not obtain audit levels.")
  public List<KeyValue<String, String>> getLevels() {
    return ReflectionUtils.getStaticFieldsValue(AppConstants.Audit.Level.class);
  }

}
