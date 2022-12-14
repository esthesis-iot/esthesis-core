package esthesis.common.jackson;

import static esthesis.common.jackson.PrivateKeySerializer.PRIVATE_KEY;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.security.PrivateKey;
import lombok.SneakyThrows;

public class PrivateKeyDeserializer extends StdDeserializer<PrivateKey> {

  protected PrivateKeyDeserializer(Class<?> vc) {
    super(vc);
  }

  @SneakyThrows
  @Override
  public PrivateKey deserialize(JsonParser p, DeserializationContext ctxt) {
    ObjectCodec oc = p.getCodec();
    JsonNode node = oc.readTree(p);
    final String privateKey = node.get(PRIVATE_KEY).asText();

    return SerDerUtils.getPrivateKey(privateKey);
  }
}
