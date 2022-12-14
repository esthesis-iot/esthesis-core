package esthesis.dataflows.commandreplyupdater.service;

import esthesis.avro.EsthesisCommandReplyMessage;
import esthesis.avro.ReplyType;
import esthesis.common.entity.CommandReplyEntity;
import java.time.Instant;
import javax.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;

@Slf4j
@ApplicationScoped
public class CommandReplyUpdaterService {

  public void createMongoEntity(Exchange exchange) {
    EsthesisCommandReplyMessage msg = exchange.getIn()
        .getBody(EsthesisCommandReplyMessage.class);
    log.debug("Received EsthesisCommandReplyMessage '{}'.", msg);

    CommandReplyEntity commandReplyEntity = new CommandReplyEntity();
    commandReplyEntity.setCorrelationId(msg.getCorrelationId());
    commandReplyEntity.setCreatedOn(Instant.parse(msg.getSeenAt()));
    commandReplyEntity.setHardwareId(msg.getHardwareId());
    commandReplyEntity.setOutput(msg.getPayload());
    commandReplyEntity.setSuccess(msg.getType() == ReplyType.s);
    log.debug("Parsed CommandReply reply '{}'.", commandReplyEntity);

    exchange.getIn().setBody(commandReplyEntity.asDocument());
  }
}
