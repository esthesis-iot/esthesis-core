package esthesis.platform.server.service;

import esthesis.platform.server.dto.device.CommandReplyDTO;
import esthesis.platform.server.mapper.CommandReplyMapper;
import esthesis.platform.server.model.CommandReply;
import esthesis.platform.server.repository.CommandReplyRepository;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Log
@Service
@Validated
@Transactional
public class CommandReplyService extends BaseService<CommandReplyDTO, CommandReply> {

  private final CommandReplyRepository commandReplyRepository;
  private final CommandReplyMapper commandReplyMapper;

  public CommandReplyService(CommandReplyRepository commandReplyRepository,
    CommandReplyMapper commandReplyMapper) {
    this.commandReplyRepository = commandReplyRepository;
    this.commandReplyMapper = commandReplyMapper;
  }

  public CommandReplyDTO findByCommandRequestId(long commandRequestId) {
    return commandReplyMapper.map(commandReplyRepository.findByCommandRequestId(commandRequestId));
  }
}
