package esthesis.service.command.impl.repository;

import esthesis.service.command.entity.CommandRequestEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CommandRequestRepository implements
    PanacheMongoRepository<CommandRequestEntity> {

}
