package esthesis.services.campaign.impl.worker;

import javax.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class DebugWorker {

  public void debug(String msg) {
    log.debug(msg);
  }
}
