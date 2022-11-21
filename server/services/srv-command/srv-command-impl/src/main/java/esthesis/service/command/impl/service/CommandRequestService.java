package esthesis.service.command.impl.service;

import esthesis.service.command.dto.CommandRequest;
import esthesis.service.common.BaseService;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
class CommandRequestService extends BaseService<CommandRequest> {

  /**
   * Purges all commands older than the given duration.
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
