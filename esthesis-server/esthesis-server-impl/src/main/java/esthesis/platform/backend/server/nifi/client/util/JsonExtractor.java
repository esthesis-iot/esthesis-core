package esthesis.platform.backend.server.nifi.client.util;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

@UtilityClass
public class JsonExtractor {

  private static final Configuration conf;

  // Setup the configuration for JsonPath.
  static {
    conf = Configuration
      .builder()
      .mappingProvider(new JacksonMappingProvider())
      .jsonProvider(new JacksonJsonProvider())
      .build();
  }

  public static <T> T extract(String json, String jsonPath, TypeRef<T> typeReference) {
    if (StringUtils.isBlank(json)) {
      return null;
    } else {
      return JsonPath.using(conf).parse(json).read(jsonPath, typeReference);
    }
  }

  public static <T> T extract(String json, String jsonPath) {
    return extract(json, jsonPath, new TypeRef<T>() {
    });
  }
}
