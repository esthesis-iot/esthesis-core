package esthesis.platform.server.repository;

import esthesis.platform.server.model.CommandReply;
import org.springframework.stereotype.Repository;

@Repository
public interface CommandReplyRepository extends BaseRepository<CommandReply> {
  CommandReply findByCommandRequestId(long commandRequestId);
}
