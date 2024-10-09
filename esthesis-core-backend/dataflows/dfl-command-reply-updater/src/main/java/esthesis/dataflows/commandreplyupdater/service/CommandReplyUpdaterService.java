package esthesis.dataflows.commandreplyupdater.service;

import esthesis.common.avro.EsthesisCommandReplyMessage;
import esthesis.common.avro.ReplyType;
import esthesis.service.command.entity.CommandReplyEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;

@Slf4j
@Transactional
@ApplicationScoped
public class CommandReplyUpdaterService {
	/**
	 * Set the output of the command reply entity. If the output is larger than MongoDB maximum size,
	 * it will be truncated and the `isTrimmed` field will be set.
	 *
	 * @param commandReplyEntity the command reply entity.
	 * @param output             the output to set.
	 */
	private void setOutput(CommandReplyEntity commandReplyEntity, String output) {
		// MongoDB maximum document size, minus 1MB for command reply metadata.
		final int MONGODB_MAX_DOC_SIZE = 15 * 1024 * 1024;

		if (output.length() > MONGODB_MAX_DOC_SIZE) {
			commandReplyEntity.setOutput(output.substring(0, MONGODB_MAX_DOC_SIZE));
			commandReplyEntity.setTrimmed(true);
		} else {
			commandReplyEntity.setOutput(output);
			commandReplyEntity.setTrimmed(false);
		}
	}

	public void createMongoEntity(Exchange exchange) {
		EsthesisCommandReplyMessage msg = exchange.getIn()
			.getBody(EsthesisCommandReplyMessage.class);
		log.debug("Received EsthesisCommandReplyMessage '{}'.", msg);

		CommandReplyEntity commandReplyEntity = new CommandReplyEntity();
		commandReplyEntity.setCorrelationId(msg.getCorrelationId());
		commandReplyEntity.setCreatedOn(Instant.parse(msg.getSeenAt()));
		commandReplyEntity.setHardwareId(msg.getHardwareId());
		setOutput(commandReplyEntity, msg.getPayload());
		commandReplyEntity.setSuccess(msg.getType() == ReplyType.s);
		log.debug("Parsed CommandReply reply '{}'.", commandReplyEntity);

		exchange.getIn().setBody(commandReplyEntity.asDocument());
	}
}
