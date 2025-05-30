package esthesis.core.common.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.security.PrivateKey;

/**
 * A custom Jackson serializer for {@link PrivateKey} objects.
 */
public class PrivateKeySerializer extends StdSerializer<PrivateKey> {

	public static final String PRIVATE_KEY = "privateKey";

	protected PrivateKeySerializer(Class<PrivateKey> t) {
		super(t);
	}

	@Override
	public void serialize(PrivateKey value, JsonGenerator gen, SerializerProvider provider)
	throws IOException {
		gen.writeStartObject();
		gen.writeFieldName(PRIVATE_KEY);
		gen.writeBinary(value.getEncoded());
		gen.writeEndObject();
	}
}
