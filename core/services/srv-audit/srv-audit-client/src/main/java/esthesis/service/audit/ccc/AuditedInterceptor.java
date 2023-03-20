package esthesis.service.audit.ccc;

import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.common.AppConstants.Audit.Category;
import esthesis.common.AppConstants.Audit.Operation;
import esthesis.service.audit.entity.AuditEntity;
import esthesis.service.audit.resource.AuditResource;
import io.quarkus.arc.Priority;
import java.lang.reflect.Method;
import java.time.Instant;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
@Audited(op = Operation.OTHER, cat = Category.NULL, msg = "")
public class AuditedInterceptor {

  @Inject
  JsonWebToken jwt;
  @Inject
  @RestClient
  AuditResource auditResource;
  @Inject
  ObjectMapper mapper;
  private int maxRequestSize = 16 * 1024 * 1000;

  @AroundInvoke
  Object audit(InvocationContext ctx) throws Exception {
    // Extract annotation parameters.
    Method method = ctx.getMethod();
    Audited audited = method.getAnnotation(Audited.class);

    // Create audit entry.
    AuditEntity auditEntity = new AuditEntity()
        .setCategory(audited.cat())
        .setOperation(audited.op())
        .setMessage(audited.msg())
        .setCreatedOn(Instant.now())
        .setCreatedBy(jwt.getName());

    // Capture before data.
    if (audited.log() == Audited.AuditLogType.DATA_IN
        || audited.log() == Audited.AuditLogType.DATA_ALL) {
      StringBuilder beforeValue = new StringBuilder();
      int i = 0;
      for (Object parameter : ctx.getParameters()) {
        beforeValue
            .append("\"arg")
            .append(i)
            .append("\":")
            .append(mapper.writeValueAsString(parameter))
            .append(",");
        i++;
      }
      if (beforeValue.length() > 0) {
        beforeValue.deleteCharAt(beforeValue.length() - 1);
        beforeValue.insert(0, "{");
        beforeValue.append("}");
        if (beforeValue.length() > maxRequestSize) {
          beforeValue = new StringBuilder(beforeValue.substring(0, maxRequestSize));
        }
        auditEntity.setValueIn(beforeValue.toString());
      }
    }

    // Execute the method.
    Object reply = ctx.proceed();

    // Capture after data.
    if (reply != null && (audited.log() == Audited.AuditLogType.DATA_OUT
        || audited.log() == Audited.AuditLogType.DATA_ALL)) {
      String replyJson = mapper.writeValueAsString(reply);
      if (replyJson.length() > maxRequestSize) {
        replyJson = replyJson.substring(0, maxRequestSize);
      }
      auditEntity.setValueOut(replyJson);
    }

    if (auditEntity.getValueIn() != null) {
      log.trace("Data in size: {}", auditEntity.getValueIn().length());
      log.trace("Data in : {}", auditEntity.getValueIn());
    } else {
      log.trace("Data in is null");
    }
    if (auditEntity.getValueOut() != null) {
      log.trace("Data out size: {}", auditEntity.getValueOut().length());
      log.trace("Data out : {}", auditEntity.getValueOut());
    } else {
      log.trace("Data out is null");
    }

    auditResource.save(auditEntity);

    return reply;
  }

}
