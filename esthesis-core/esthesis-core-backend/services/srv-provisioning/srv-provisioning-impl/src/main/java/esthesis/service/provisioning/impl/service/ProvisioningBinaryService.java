package esthesis.service.provisioning.impl.service;

import esthesis.service.common.BaseService;
import esthesis.service.provisioning.entity.ProvisioningPackageBinaryEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
@ApplicationScoped
public class ProvisioningBinaryService extends BaseService<ProvisioningPackageBinaryEntity> {

}
