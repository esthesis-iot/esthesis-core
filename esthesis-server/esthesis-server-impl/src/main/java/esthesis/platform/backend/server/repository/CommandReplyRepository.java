package esthesis.platform.backend.server.repository;

import esthesis.platform.backend.server.model.CommandReply;
import org.springframework.stereotype.Repository;

@Repository
public interface CommandReplyRepository extends BaseRepository<CommandReply> {
  CommandReply findByCommandRequestId(long commandRequestId);
}
