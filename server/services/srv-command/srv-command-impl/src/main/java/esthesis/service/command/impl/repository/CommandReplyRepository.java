package esthesis.service.command.impl.repository;

import esthesis.common.entity.CommandReplyEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CommandReplyRepository implements
    PanacheMongoRepository<CommandReplyEntity> {

}
