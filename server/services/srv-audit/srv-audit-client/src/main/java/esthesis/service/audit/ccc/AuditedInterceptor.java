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
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Audited(op = Operation.NULL, cat = Category.NULL, msg = "")
@Priority(1)
@Interceptor
public class AuditedInterceptor {

  @Inject
  JsonWebToken jwt;

  @Inject
  @RestClient
  AuditResource auditResource;

  @Inject
  ObjectMapper mapper;

  @AroundInvoke
  Object audit(InvocationContext ctx) throws Exception {
    // Extract annotation parameters.
    Method method = ctx.getMethod();
    Audited audited = method.getAnnotation(Audited.class);

    // Capture before data.
    String beforeValue = "";
    for (Object parameter : ctx.getParameters()) {
      beforeValue += mapper.writeValueAsString(parameter) + "\n";
    }

    // Execute the method.
    Object reply = ctx.proceed();

    // Create audit entry.
    AuditEntity auditEntity = new AuditEntity()
        .setCategory(audited.cat())
        .setOperation(audited.op())
        .setMessage(audited.msg())
        .setCreatedOn(Instant.now())
        .setCreatedBy(jwt.getName());
    if (StringUtils.isNotBlank(beforeValue)) {
      auditEntity.setValueIn(beforeValue);
    }
    if (reply != null) {
      auditEntity.setValueOut(mapper.writeValueAsString(reply));
    }
    auditResource.save(auditEntity);

    return reply;
  }

}
