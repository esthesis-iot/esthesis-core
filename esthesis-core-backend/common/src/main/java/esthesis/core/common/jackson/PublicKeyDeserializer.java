package esthesis.core.common.jackson;

import static esthesis.core.common.jackson.PublicKeySerializer.PUBLIC_KEY;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.security.PublicKey;
import lombok.SneakyThrows;

/**
 * A custom Jackson deserializer for public keys to be parsed to {@link PublicKey} objects.
 */
public class PublicKeyDeserializer extends StdDeserializer<PublicKey> {

	protected PublicKeyDeserializer(Class<?> vc) {
		super(vc);
	}

	@SneakyThrows
	@Override
	public PublicKey deserialize(JsonParser p, DeserializationContext ctxt) {
		ObjectCodec oc = p.getCodec();
		JsonNode node = oc.readTree(p);
		final String publicKey = node.get(PUBLIC_KEY).asText();

		return SerDerUtils.getPublicKey(publicKey);
	}
}
