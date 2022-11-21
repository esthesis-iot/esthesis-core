package esthesis.service.command.impl.service;

import esthesis.common.dto.CommandReply;
import esthesis.service.common.BaseService;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
class CommandReplyService extends BaseService<CommandReply> {

  public CommandReply findByCorrelationId(String correlationId) {
    return findFirstByColumn("correlationId", correlationId);
  }

  /**
   * Purges all replies older than the given duration.
   *
   * @param duration The duration, in days.
   */
  public void purge(Optional<Integer> duration) {
    if (duration.isEmpty()) {
      deleteAll();
    } else {
      getRepository().delete(
          "createdOn <= ?1",
          Instant.now().minus(Duration.ofDays(duration.get())));
    }
  }

}
