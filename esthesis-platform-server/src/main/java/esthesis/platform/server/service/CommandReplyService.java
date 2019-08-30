package esthesis.platform.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.common.device.control.MqttCommandReplyPayload;
import esthesis.platform.server.dto.CommandReplyDTO;
import esthesis.platform.server.events.CommandReplyEvent;
import esthesis.platform.server.mapper.CommandReplyMapper;
import esthesis.platform.server.model.CommandReply;
import esthesis.platform.server.repository.CommandReplyRepository;
import lombok.extern.java.Log;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.util.logging.Level;

@Log
@Service
@Validated
@Transactional
public class CommandReplyService extends BaseService<CommandReplyDTO, CommandReply> {

  private final ObjectMapper objectMapper;
  private final CommandReplyRepository commandReplyRepository;
  private final CommandReplyMapper commandReplyMapper;

  public CommandReplyService(ObjectMapper objectMapper,
    CommandReplyRepository commandReplyRepository,
    CommandReplyMapper commandReplyMapper) {
    this.objectMapper = objectMapper;
    this.commandReplyRepository = commandReplyRepository;
    this.commandReplyMapper = commandReplyMapper;
  }

  @EventListener
  public void onApplicationEvent(CommandReplyEvent event) {
    try {
      // Convert the payload received.
      MqttCommandReplyPayload payload = objectMapper
        .readValue(event.getMqttDataEvent().getPayload(), MqttCommandReplyPayload.class);

      // Persist the reply.
      CommandReplyDTO commandReplyDTO = new CommandReplyDTO();
      commandReplyDTO.setCommandRequest(payload.getCommandId());
      commandReplyDTO.setPayload(payload.getPayload());
      save(commandReplyDTO);
    } catch (IOException e) {
      log.log(Level.SEVERE, "Could not process command reply.", e);
    }
  }

  public CommandReplyDTO findByCommandRequestId(long commandRequestId) {
    return commandReplyMapper.map(commandReplyRepository.findByCommandRequestId(commandRequestId));
  }
}
