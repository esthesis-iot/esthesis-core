package esthesis.services.settings.impl.repository;

import esthesis.service.settings.entity.SettingEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SettingsRepository implements PanacheMongoRepository<SettingEntity> {

}
