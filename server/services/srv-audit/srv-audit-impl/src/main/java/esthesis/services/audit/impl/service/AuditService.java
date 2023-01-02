package esthesis.services.audit.impl.service;

import esthesis.service.audit.entity.AuditEntity;
import esthesis.service.common.BaseService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Slf4j
@ApplicationScoped
public class AuditService extends BaseService<AuditEntity> {

  @Inject
  JsonWebToken jwt;

}
