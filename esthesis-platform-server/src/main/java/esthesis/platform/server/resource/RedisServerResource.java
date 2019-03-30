package esthesis.platform.server.resource;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import com.eurodyn.qlack.util.querydsl.EmptyPredicateCheck;
import com.querydsl.core.types.Predicate;
import esthesis.platform.server.dto.RedisServerDTO;
import esthesis.platform.server.model.RedisServer;
import esthesis.platform.server.service.RedisServerService;
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

@RestController
@RequestMapping("/redis-server")
@Validated
public class RedisServerResource {

  private final RedisServerService redisServerService;

  public RedisServerResource(RedisServerService redisServerService) {
    this.redisServerService = redisServerService;
  }


  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not retrieve Redis servers list.")
  @EmptyPredicateCheck
  public Page<RedisServerDTO> findAll(@QuerydslPredicate(root = RedisServer.class) Predicate predicate,
      Pageable pageable) {
    return redisServerService.findAll(predicate, pageable);
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not save Redis server.")
  public RedisServerDTO save(@Valid @RequestBody RedisServerDTO redisServerDTO) {
    return redisServerService.save(redisServerDTO);
  }

  @GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not fetch Redis server.")
  public RedisServerDTO get(@PathVariable long id) {
    return redisServerService.findById(id);
  }

  @DeleteMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not delete tag.")
  public void delete(@PathVariable long id) {
    redisServerService.deleteById(id);
  }
}
