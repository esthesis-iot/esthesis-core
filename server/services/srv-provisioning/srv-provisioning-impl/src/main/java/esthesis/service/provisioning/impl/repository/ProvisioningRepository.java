package esthesis.service.provisioning.impl.repository;

import esthesis.service.provisioning.dto.ProvisioningPackage;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProvisioningRepository implements PanacheMongoRepository<ProvisioningPackage> {

}
