package esthesis.platform.server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.platform.server.config.AppProperties;
import esthesis.platform.server.dto.AboutDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
class AboutServiceTest {

  @Spy
  ObjectMapper objectMapper;

  @Spy
  AppProperties appProperties;

  @InjectMocks
  private AboutService aboutService;

  @Test
  void getAbout() throws IOException {
    final AboutDTO about = aboutService.getAbout();
    assertNotNull(about);
    assertEquals("https://github.com/esthesis-iot/esthesis-platform", about.getRemoteOriginUrl());
  }
}
