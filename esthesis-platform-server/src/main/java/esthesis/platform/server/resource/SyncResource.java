package esthesis.platform.server.resource;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import esthesis.platform.server.service.SyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@Validated
@RequestMapping("/sync")
@RequiredArgsConstructor
public class SyncResource {

  private final SyncService syncService;

  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Syncing was unsuccessful.")
  public ResponseEntity sync() throws IOException {
    syncService.sync();
    return ResponseEntity.ok().build();
  }

  @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not delete NiFi "
    + "Workflow.")
  public ResponseEntity delete() throws IOException {
    syncService.deleteWorkflow();
    return ResponseEntity.ok().build();
  }

}
