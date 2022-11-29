package esthesis.common.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.time.Instant;

public class MongoInstantDeserializer extends JsonDeserializer<Instant> {

  @Override
  public Instant deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
  throws IOException {
    JsonNode node = jsonParser.readValueAsTree();
    return Instant.parse(node.get("$date").asText());
  }
}
