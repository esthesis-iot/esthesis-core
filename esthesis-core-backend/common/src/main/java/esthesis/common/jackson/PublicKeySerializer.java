package esthesis.common.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.security.PublicKey;

public class PublicKeySerializer extends StdSerializer<PublicKey> {

  public static final String PUBLIC_KEY = "publicKey";

  protected PublicKeySerializer(Class<PublicKey> t) {
    super(t);
  }

  @Override
  public void serialize(PublicKey value, JsonGenerator gen, SerializerProvider provider)
  throws IOException {
    gen.writeStartObject();
    gen.writeFieldName(PUBLIC_KEY);
    gen.writeBinary(value.getEncoded());
    gen.writeEndObject();
  }
}
