package esthesis.platform.server.resource;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import com.eurodyn.qlack.util.data.filter.ReplyFilter;
import com.eurodyn.qlack.util.data.filter.ReplyPageableFilter;
import com.eurodyn.qlack.util.querydsl.EmptyPredicateCheck;
import com.querydsl.core.types.Predicate;
import esthesis.common.device.commands.CommandReplyDTO;
import esthesis.common.device.commands.CommandRequestDTO;
import esthesis.common.device.dto.DeviceDTO;
import esthesis.platform.server.dto.CommandExecuteOrderDTO;
import esthesis.platform.server.model.CommandRequest;
import esthesis.platform.server.service.CommandReplyService;
import esthesis.platform.server.service.CommandRequestService;
import esthesis.platform.server.service.DTService;
import esthesis.platform.server.service.DeviceService;
import javax.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.stream.Stream;

@Validated
@RestController
@RequestMapping("/command")
public class CommandResource {

  private final CommandRequestService commandRequestService;
  private final CommandReplyService commandReplyService;
  private final DTService dtService;
  private final DeviceService deviceService;

  public CommandResource(
    CommandRequestService commandRequestService,
    CommandReplyService commandReplyService, DTService dtService,
    DeviceService deviceService) {
    this.commandRequestService = commandRequestService;
    this.commandReplyService = commandReplyService;
    this.dtService = dtService;
    this.deviceService = deviceService;
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class,
    logMessage = "There was a problem retrieving commands.")
  @ReplyPageableFilter("operation,description,deviceHardwareId,id,createdOn")
  @EmptyPredicateCheck
  public Page<CommandRequestDTO> findAll(
    @QuerydslPredicate(root = CommandRequest.class) Predicate predicate, Pageable pageable) {
    return commandRequestService.findAll(predicate, pageable);
  }

  @GetMapping(path = "reply", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not fetch command reply.")
  @ReplyFilter("-createdBy")
  public CommandReplyDTO getReply(@RequestParam long requestId) {
    return commandReplyService.findByCommandRequestId(requestId);
  }

  @Async
  @PostMapping(path = "execute")
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not execute command.")
  public void executeCommand(@Valid @RequestBody CommandExecuteOrderDTO commandExecuteOrderDTO) {
    // Collect all devices that should receive the command.
    Stream.concat(
      deviceService
        .findByTags(Arrays.asList(commandExecuteOrderDTO.getTags().split(",")))
        .stream()
        .map(DeviceDTO::getHardwareId),
      deviceService
        .findByHardwareIds(Arrays.asList(commandExecuteOrderDTO.getHardwareIds().split(",")))
        .stream()
        .map(DeviceDTO::getHardwareId)
    ).forEach(hardwareId -> {
      dtService.executeCommand(hardwareId, commandExecuteOrderDTO.getCommand(),
        commandExecuteOrderDTO.getDescription(), commandExecuteOrderDTO.getArguments());
    });
  }
}
