package esthesis.service.provisioning.impl.repository;

import esthesis.service.provisioning.dto.ProvisioningPackageBinary;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProvisioningBinaryRepository implements
    PanacheMongoRepository<ProvisioningPackageBinary> {

}
