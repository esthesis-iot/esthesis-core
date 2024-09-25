package esthesis.core.common.jackson;

import static esthesis.core.common.jackson.KeyPairSerializer.PRIVATE_KEY;
import static esthesis.core.common.jackson.KeyPairSerializer.PUBLIC_KEY;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.security.KeyPair;
import lombok.SneakyThrows;

public class KeyPairDeserializer extends StdDeserializer<KeyPair> {

  protected KeyPairDeserializer(Class<?> vc) {
    super(vc);
  }

  @SneakyThrows
  @Override
  public KeyPair deserialize(JsonParser p, DeserializationContext ctxt) {
    ObjectCodec oc = p.getCodec();
    JsonNode node = oc.readTree(p);
    final String publicKey = node.get(PUBLIC_KEY).asText();
    final String privateKey = node.get(PRIVATE_KEY).asText();

    return new KeyPair(SerDerUtils.getPublicKey(publicKey), SerDerUtils.getPrivateKey(privateKey));
  }
}
