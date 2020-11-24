package esthesis.platform.backend.server.resource;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import com.eurodyn.qlack.util.data.filter.ReplyFilter;
import com.eurodyn.qlack.util.data.filter.ReplyPageableFilter;
import com.eurodyn.qlack.util.querydsl.EmptyPredicateCheck;
import com.querydsl.core.types.Predicate;
import esthesis.platform.backend.common.device.commands.CommandReplyDTO;
import esthesis.platform.backend.common.device.commands.CommandRequestDTO;
import esthesis.platform.backend.common.device.dto.DeviceDTO;
import esthesis.platform.backend.server.dto.CommandExecuteOrderDTO;
import esthesis.platform.backend.server.model.CommandRequest;
import esthesis.platform.backend.server.service.CommandReplyService;
import esthesis.platform.backend.server.service.CommandRequestService;
import esthesis.platform.backend.server.service.DTService;
import esthesis.platform.backend.server.service.DeviceService;
import javax.validation.Valid;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
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

import java.text.MessageFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Log
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

  @GetMapping(path = "reply-sync", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not fetch command reply.")
  @ReplyFilter("-createdBy")
  public CommandReplyDTO getReplySync(@RequestParam long requestId,
    @RequestParam(defaultValue = "5000") long msec) throws InterruptedException {
    CommandReplyDTO reply = null;
    Instant start = Instant.now();
    boolean cb = false;
    while (start.plus(msec, ChronoUnit.MILLIS).isAfter(Instant.now()) && !cb) {
      reply = commandReplyService.findByCommandRequestId(requestId);
      if (reply != null) {
        cb = true;
      } else {
        Thread.sleep(1000);
      }
    }

    if (!cb) {
      log.fine(MessageFormat.format("Command reply timed out after waiting for {0} msec.", msec));
    }

    return reply;
  }

  @PostMapping(path = "execute-sync")
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not execute command.")
  public List<String> executeCommandSync(
    @Valid @RequestBody CommandExecuteOrderDTO commandExecuteOrderDTO) {
    return executeCommand(commandExecuteOrderDTO);
  }

  @Async
  @PostMapping(path = "execute")
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not execute command.")
  public List<String> executeCommand(
    @Valid @RequestBody CommandExecuteOrderDTO commandExecuteOrderDTO) {
    List<String> requestIds = new ArrayList<>();

    // Collect all devices that should receive the command.
    Stream.concat(
      deviceService
        .findByTags(Arrays
          .asList(StringUtils.defaultIfEmpty(commandExecuteOrderDTO.getTags(), "").split(",")))
        .stream()
        .map(DeviceDTO::getHardwareId),
      deviceService
        .findByHardwareIds(Arrays.asList(
          StringUtils.defaultIfEmpty(commandExecuteOrderDTO.getHardwareIds(), "").split(",")))
        .stream()
        .map(DeviceDTO::getHardwareId)
    ).forEach(hardwareId -> requestIds
      .add(dtService.executeCommand(hardwareId, commandExecuteOrderDTO.getCommand(),
        commandExecuteOrderDTO.getDescription(), commandExecuteOrderDTO.getArguments())));

    return requestIds;
  }
}
