package esthesis.services.application.impl.repository;

import esthesis.service.application.entity.ApplicationEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ApplicationRepository implements
    PanacheMongoRepository<ApplicationEntity> {

}
