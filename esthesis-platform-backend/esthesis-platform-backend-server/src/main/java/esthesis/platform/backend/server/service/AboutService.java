package esthesis.platform.backend.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.platform.backend.server.config.AppProperties;
import esthesis.platform.backend.server.dto.AboutDTO;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;

/**
 * Provides 'about' information on esthesis platform.
 */
@Log
@Service
@Validated
public class AboutService {

  private final ObjectMapper objectMapper;
  private final AppProperties appProperties;

  public AboutService(ObjectMapper objectMapper,
    AppProperties appProperties) {
    this.objectMapper = objectMapper;
    this.appProperties = appProperties;
  }

  /**
   * Generate about information.
   */
  public AboutDTO getAbout() throws IOException {
    // Get versioning info.
    AboutDTO aboutDTO = objectMapper
      .readValue(this.getClass().getResourceAsStream("/git.json"), AboutDTO.class);

    // Get system info.
    aboutDTO.setNodeId(appProperties.getNodeId());

    return aboutDTO;
  }
}
