package esthesis.device.runtime.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.device.runtime.dto.AboutDTO;
import java.io.IOException;
import lombok.extern.java.Log;

//TODO Port to QLACK

/**
 * Provides 'about' information on esthesis platform.
 */
@Log
public class AboutService {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Generate about information.
   */
  public static AboutDTO getAbout() throws IOException {
    // Get versioning info.
    AboutDTO aboutDTO = objectMapper
      .readValue(Thread.currentThread().getContextClassLoader().getResourceAsStream("/git.json"),
        AboutDTO.class);

    return aboutDTO;
  }
}
