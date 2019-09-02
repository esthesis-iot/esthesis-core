package esthesis.platform.server.resource;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import com.eurodyn.qlack.util.data.filter.ReplyFilter;
import com.eurodyn.qlack.util.data.filter.ReplyPageableFilter;
import com.eurodyn.qlack.util.querydsl.EmptyPredicateCheck;
import com.querydsl.core.types.Predicate;
import esthesis.common.config.AppConstants.MqttCommand;
import esthesis.platform.server.dto.CommandReplyDTO;
import esthesis.platform.server.dto.CommandRequestDTO;
import esthesis.platform.server.dto.CommandSpecificationDTO;
import esthesis.platform.server.model.Application;
import esthesis.platform.server.repository.DeviceRepository;
import esthesis.platform.server.service.CommandReplyService;
import esthesis.platform.server.service.CommandRequestService;
import javax.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@Validated
@RequestMapping("/control")
public class ControlResource {

  private final CommandRequestService commandRequestService;
  private final DeviceRepository deviceRepository;
  private final CommandReplyService commandReplyService;

  public ControlResource(
    CommandRequestService commandRequestService,
    DeviceRepository deviceRepository,
    CommandReplyService commandReplyService) {
    this.commandRequestService = commandRequestService;
    this.deviceRepository = deviceRepository;
    this.commandReplyService = commandReplyService;
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class,
    logMessage = "There was a problem retrieving commands.")
  @ReplyPageableFilter("command,description,device,id,hardwareId,createdOn")
  @EmptyPredicateCheck
  public Page<CommandRequestDTO> findAll(
    @QuerydslPredicate(root = Application.class) Predicate predicate,
    Pageable pageable) {
    //noinspection OptionalGetWithoutIsPresent
    return commandRequestService.findAll(predicate, pageable).map(
      commandRequestDTO -> commandRequestDTO.setHardwareId(
        deviceRepository.findById(commandRequestDTO.getDevice()).get().getHardwareId()));
  }

  @GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not fetch application.")
  @ReplyFilter("command,description,device,id")
  public CommandRequestDTO get(@PathVariable long id) {
    return commandRequestService.findById(id);
  }

  @PostMapping(path = "execute", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not execute command.")
  public ResponseEntity execute(@Valid @RequestBody CommandSpecificationDTO cmd) {
    commandRequestService.execute(cmd);

    return ResponseEntity.ok().build();
  }

  /**
   * Returns the list of commands a device may react to. Note that according to the runtime client
   * each device is running not all commands may be understood.
   */
  @GetMapping(path = "commands", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<String> commands() {
    return Stream.of(MqttCommand.values()).map(Enum::name).sorted().collect(Collectors.toList());
  }

  @GetMapping(path = "reply", produces = MediaType.APPLICATION_JSON_VALUE)
  public CommandReplyDTO getReply(@RequestParam long id) {
    return new CommandReplyDTO()
      .setCommandRequest(id)
      .setPayload(commandReplyService.findByCommandRequestId(id).getPayload());
  }

}
