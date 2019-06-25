package esthesis.platform.server.service;

import esthesis.platform.server.dto.CommandReplyDTO;
import esthesis.platform.server.model.CommandReply;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@Transactional
public class CommandReplyService extends BaseService<CommandReplyDTO, CommandReply> {

}
