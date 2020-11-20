package esthesis.backend.resource;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import esthesis.backend.dto.AboutDTO;
import esthesis.backend.service.AboutService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Validated
@RestController
@RequestMapping("/about")
public class AboutResource {

  private final AboutService aboutService;

  public AboutResource(AboutService aboutService) {
    this.aboutService = aboutService;
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not fetch about data.")
  public AboutDTO getAbout() throws IOException {
    return aboutService.getAbout();
  }
}
