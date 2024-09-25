package esthesis.core.common.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.time.Instant;

/**
 * A custom Jackson deserializer for MongoDB $date attributes to be parsed to {@link Instant}
 * objects.
 */
public class MongoInstantDeserializer extends JsonDeserializer<Instant> {

  @Override
  public Instant deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
  throws IOException {
    JsonNode node = jsonParser.readValueAsTree();

    // The node might be coming directly from a Mongo serialization or an object serialization. We
    // need to differentiate between the two.
    if (node.get("$date") != null) {
      return Instant.parse(node.get("$date").asText());
    } else {
      return Instant.parse(node.asText());
    }
  }
}
