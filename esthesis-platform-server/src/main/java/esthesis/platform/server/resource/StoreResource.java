package esthesis.platform.server.resource;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import com.eurodyn.qlack.util.querydsl.EmptyPredicateCheck;
import com.querydsl.core.types.Predicate;
import esthesis.platform.server.dto.StoreDTO;
import esthesis.platform.server.model.Store;
import esthesis.platform.server.service.StoreService;
import javax.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/stores")
public class StoreResource {

  private final StoreService storeService;

  public StoreResource(StoreService storeService) {
    this.storeService = storeService;
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not retrieve stores list.")
  @EmptyPredicateCheck
  public Page<StoreDTO> findAll(@QuerydslPredicate(root = Store.class) Predicate predicate,
    Pageable pageable) {
    return storeService.findAll(predicate, pageable);
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not save store.")
  public StoreDTO save(@Valid @RequestBody StoreDTO storeDTO) {
    return storeService.save(storeDTO);
  }

  @GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not fetch store.")
  public StoreDTO get(@PathVariable long id) {
    return storeService.findById(id);
  }

  @DeleteMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not delete store.")
  public void delete(@PathVariable long id) {
    storeService.deleteById(id);
  }
}
