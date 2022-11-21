package esthesis.service.command.impl.service;

import esthesis.common.dto.CommandReply;
import esthesis.service.common.BaseService;
import javax.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
class CommandReplyService extends BaseService<CommandReply> {

  public CommandReply findByCorrelationId(String correlationId) {
    return findFirstByColumn("correlationId", correlationId);
  }
}
