package esthesis.service.provisioning.impl.repository;

import esthesis.service.provisioning.entity.ProvisioningPackageBinaryEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProvisioningBinaryRepository implements
	PanacheMongoRepository<ProvisioningPackageBinaryEntity> {

}
