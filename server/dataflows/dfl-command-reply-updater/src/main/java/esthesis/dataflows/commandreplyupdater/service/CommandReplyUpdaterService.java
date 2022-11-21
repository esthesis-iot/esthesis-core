package esthesis.dataflows.commandreplyupdater.service;

import esthesis.avro.EsthesisCommandReplyMessage;
import esthesis.avro.ReplyType;
import esthesis.common.dto.CommandReply;
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

    CommandReply commandReply = new CommandReply();
    commandReply.setCorrelationId(msg.getCorrelationId());
    commandReply.setCreatedOn(Instant.parse(msg.getSeenAt()));
    commandReply.setHardwareId(msg.getHardwareId());
    commandReply.setOutput(msg.getPayload());
    commandReply.setSuccess(msg.getType() == ReplyType.s);
    log.debug("Parsed CommandReply reply '{}'.", commandReply);

    exchange.getIn().setBody(commandReply.asDocument());
  }
}
