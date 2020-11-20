package esthesis.backend.repository;

import esthesis.backend.model.CommandReply;
import org.springframework.stereotype.Repository;

@Repository
public interface CommandReplyRepository extends BaseRepository<CommandReply> {
  CommandReply findByCommandRequestId(long commandRequestId);
}
