package esthesis.backend.service;

import esthesis.common.device.commands.CommandRequestDTO;
import esthesis.backend.model.CommandRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@Transactional
public class CommandRequestService extends BaseService<CommandRequestDTO, CommandRequest> {

  public CommandRequestService() {
    super();
  }
}
