package esthesis.core.common.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.security.KeyPair;

public class KeyPairSerializer extends StdSerializer<KeyPair> {

  public static final String PUBLIC_KEY = "publicKey";
  public static final String PRIVATE_KEY = "privateKey";

  protected KeyPairSerializer(Class<KeyPair> t) {
    super(t);
  }

  @Override
  public void serialize(KeyPair value, JsonGenerator gen, SerializerProvider provider)
  throws IOException {
    gen.writeStartObject();
    gen.writeFieldName(PUBLIC_KEY);
    gen.writeBinary(value.getPublic().getEncoded());
    gen.writeFieldName(PRIVATE_KEY);
    gen.writeBinary(value.getPrivate().getEncoded());
    gen.writeEndObject();
  }
}
