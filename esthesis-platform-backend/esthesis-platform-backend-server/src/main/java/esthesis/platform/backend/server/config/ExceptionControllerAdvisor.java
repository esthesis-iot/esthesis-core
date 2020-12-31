package esthesis.platform.backend.server.config;

import com.eurodyn.qlack.util.data.filter.JSONFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ExceptionControllerAdvisor {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity handleValidationException(MethodArgumentNotValidException exception) {
    return new ResponseEntity<>(JSONFilter
      .filterDefault(exception.getBindingResult().getAllErrors(),
        "defaultMessage,objectName,field,rejectedValue,code"),
      HttpStatus.BAD_REQUEST);
  }
}
