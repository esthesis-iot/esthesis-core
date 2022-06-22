package esthesis.services.device.repository;

import javax.enterprise.context.ApplicationScoped;

import esthesis.dto.Device;
import io.quarkus.mongodb.panache.PanacheMongoRepository;

@ApplicationScoped
public class DeviceRepository implements PanacheMongoRepository<Device> {

}
