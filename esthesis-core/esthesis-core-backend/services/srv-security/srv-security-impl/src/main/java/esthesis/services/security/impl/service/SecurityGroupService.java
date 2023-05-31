package esthesis.services.security.impl.service;

import esthesis.service.common.BaseService;
import esthesis.service.security.entity.GroupEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
@ApplicationScoped
public class SecurityGroupService extends BaseService<GroupEntity> {

}
