package esthesis.core.common.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.inject.Singleton;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * A global Jackson customizer to register application-wide Jackson settings.
 */
@Singleton
public class JacksonCustomizer implements ObjectMapperCustomizer {

	public void customize(ObjectMapper mapper) {
		mapper.registerModule(new SimpleModule()
			.addSerializer(PublicKey.class, new PublicKeySerializer(PublicKey.class))
			.addSerializer(PrivateKey.class, new PrivateKeySerializer(PrivateKey.class))
			.addSerializer(KeyPair.class, new KeyPairSerializer(KeyPair.class))

			.addDeserializer(PublicKey.class, new PublicKeyDeserializer(PublicKey.class))
			.addDeserializer(PrivateKey.class, new PrivateKeyDeserializer(PrivateKey.class))
			.addDeserializer(KeyPair.class, new KeyPairDeserializer(KeyPair.class))
		);
	}
}
