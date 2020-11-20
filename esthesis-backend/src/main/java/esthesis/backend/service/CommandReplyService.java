package esthesis.backend.service;

import esthesis.backend.repository.CommandReplyRepository;
import esthesis.common.device.commands.CommandReplyDTO;
import esthesis.backend.mapper.CommandReplyMapper;
import esthesis.backend.model.CommandReply;
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
