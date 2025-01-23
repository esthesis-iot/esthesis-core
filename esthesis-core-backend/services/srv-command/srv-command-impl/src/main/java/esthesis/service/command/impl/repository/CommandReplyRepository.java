package esthesis.service.command.impl.repository;

import esthesis.service.command.entity.CommandReplyEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Quarkus Panache repository for @{@link CommandReplyEntity}.
 */
@ApplicationScoped
public class CommandReplyRepository implements
	PanacheMongoRepository<CommandReplyEntity> {

}
