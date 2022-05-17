package esthesis.platform.server.resource;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.platform.server.config.AppSettings.Setting.UI;
import esthesis.platform.server.service.SettingResolverService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URL;
import java.util.Random;

@Validated
@RestController
@RequestMapping("util")
public class UtilResource {

  private final SettingResolverService srs;
  private final ObjectMapper mapper;
  private final static String DEFAULT_BG = "/assets/img/bg.jpg";

  public UtilResource(SettingResolverService srs, ObjectMapper mapper) {
    this.srs = srs;
    this.mapper = mapper;
  }

  @GetMapping(path = "bg-photo", produces = MediaType.TEXT_PLAIN_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class,
    logMessage = "Could not fetch background photo.")
  public String getBackgroundPhoto() throws IOException {
    // Check if integration with Pixabay is enabled and fetch a random photo.
    if (Boolean.parseBoolean(srs.get(UI.PIXABAY_ENABLED))) {
      final String pixabayKey = srs.get(UI.PIXABAY_KEY);
      final String pixabayCategory = srs.get(UI.PIXABAY_CATEGORY);
      final String pixabayUrl =
        "https://pixabay.com/api/?" +
          "key=" + pixabayKey + "&" +
          "image_type=photo&" +
          "orientation=horizontal&" +
          "category=" + pixabayCategory + "&" +
          "safesearch=true&" +
          "per_page=10&" +
          "order=popular&" +
          "minWidth=1024&" +
          "minHeight=768";
      final JsonNode photos = mapper.readTree(new URL(pixabayUrl));
      final int maxPhotos = photos.get("hits").size();

      return photos.get("hits").get(new Random().nextInt(maxPhotos)).get("largeImageURL").asText();
    } else {
      // If pixabay is not enabled, return the default background.
      return DEFAULT_BG;
    }
  }
}
