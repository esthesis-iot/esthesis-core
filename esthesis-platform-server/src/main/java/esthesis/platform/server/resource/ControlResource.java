package esthesis.platform.server.resource;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import com.eurodyn.qlack.util.querydsl.EmptyPredicateCheck;
import com.querydsl.core.types.Predicate;
import esthesis.platform.server.dto.CommandRequestDTO;
import esthesis.platform.server.model.Application;
import esthesis.platform.server.service.CommandRequestService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/control")
public class ControlResource {

  private final CommandRequestService commandRequestService;

  public ControlResource(
    CommandRequestService commandRequestService) {
    this.commandRequestService = commandRequestService;
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class,
    logMessage = "There was a problem retrieving commands.")
//  @ReplyPageableFilter("name,token,state,createdOn,id")
  @EmptyPredicateCheck
  public Page<CommandRequestDTO> findAll(
    @QuerydslPredicate(root = Application.class) Predicate predicate,
    Pageable pageable) {
    return commandRequestService.findAll(predicate, pageable);
  }

  @GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not fetch application.")
//  @ReplyFilter("name,token,state,createdOn,id")
  public CommandRequestDTO get(@PathVariable long id) {
    return commandRequestService.findById(id);
  }

}
