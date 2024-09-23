package esthesis.service.command.impl.repository;

import esthesis.service.command.entity.CommandReplyEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CommandReplyRepository implements
	PanacheMongoRepository<CommandReplyEntity> {

}
