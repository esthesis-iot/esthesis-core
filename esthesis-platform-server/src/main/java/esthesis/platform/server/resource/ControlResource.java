package esthesis.platform.server.resource;

import esthesis.platform.server.service.ControlService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/control")
public class ControlResource {
  private final ControlService controlService;

  public ControlResource(ControlService controlService) {
    this.controlService = controlService;
  }



//  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
//  @ExceptionWrapper(wrapper = QExceptionWrapper.class,
//    logMessage = "There was a problem retrieving commands.")
//  @ReplyPageableFilter("command,description,device,id,hardwareId,createdOn")
//  @EmptyPredicateCheck
//  public Page<CommandRequestDTO> findAll(
//    @QuerydslPredicate(root = Application.class) Predicate predicate,
//    Pageable pageable) {
//    //noinspection OptionalGetWithoutIsPresent
//    return commandRequestService.findAll(predicate, pageable).map(
//      commandRequestDTO -> commandRequestDTO.setHardwareId(
//        deviceRepository.findById(commandRequestDTO.getDevice()).get().getHardwareId()));
//  }
//
//  @GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
//  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not fetch application.")
//  @ReplyFilter("command,description,device,id")
//  public CommandRequestDTO get(@PathVariable long id) {
//    return commandRequestService.findById(id);
//  }
//
//  @PostMapping(path = "execute", produces = MediaType.APPLICATION_JSON_VALUE)
//  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not execute command.")
//  public ResponseEntity execute(@Valid @RequestBody CommandRequestDTO cmd) {
//    commandRequestService.execute(cmd);
//
//    return ResponseEntity.ok().build();
//  }

//  /**
//   * Returns the list of commands a device may react to. Note that according to the runtime client
//   * each device is running not all commands may be understood.
//   */
//  @GetMapping(path = "commands", produces = MediaType.APPLICATION_JSON_VALUE)
//  public List<String> commands() {
//    return Stream.of(MqttCommand.values()).map(Enum::name).sorted().collect(Collectors.toList());
//  }
//
//  @GetMapping(path = "reply", produces = MediaType.APPLICATION_JSON_VALUE)
//  public CommandReplyDTO getReply(@RequestParam long id) {
//    return new CommandReplyDTO()
//      .setCommandRequest(id)
//      .setPayload(commandReplyService.findByCommandRequestId(id).getPayload());
//  }

}
