package esthesis.service.command.impl.repository;

import esthesis.common.dto.CommandRequest;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CommandRequestRepository implements
    PanacheMongoRepository<CommandRequest> {

}
