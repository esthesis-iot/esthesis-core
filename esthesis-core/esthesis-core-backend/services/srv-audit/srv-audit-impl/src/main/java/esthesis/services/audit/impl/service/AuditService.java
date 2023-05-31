package esthesis.services.audit.impl.service;

import esthesis.service.audit.entity.AuditEntity;
import esthesis.service.common.BaseService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
@ApplicationScoped
public class AuditService extends BaseService<AuditEntity> {

}
