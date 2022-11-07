package esthesis.services.settings.impl.repository;

import esthesis.service.settings.dto.DevicePageField;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DevicePageFieldRepository implements
    PanacheMongoRepository<DevicePageField> {

}
