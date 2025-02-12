package esthesis.core.common.jackson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.BeanDeserializerFactory;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.deser.DeserializerFactory;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import com.fasterxml.jackson.databind.ser.Serializers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Iterator;

@ExtendWith(MockitoExtension.class)
class JacksonCustomizerTest {

	@InjectMocks
	private JacksonCustomizer jacksonCustomizer;

	@Test
	void testCustomize() {
		// Create a default Jackson JsonMapper with modules registered.
		JsonMapper mapper = JsonMapper.builder().findAndAddModules().build();

		// Apply customizations using JacksonCustomizer.
		jacksonCustomizer.customize(mapper);

		// Assert: Verify that the DeserializationContext and SerializerFactory are correctly set up.
		DeserializationContext deserializationContext = mapper.getDeserializationContext();
		DeserializerFactory factory = deserializationContext.getFactory();

		// Check that the deserialization factory uses a BeanDeserializerFactory (Jackson default).
		assertInstanceOf(BeanDeserializerFactory.class, factory);

		// Verify the expected deserialization context implementation
		assertInstanceOf(DefaultDeserializationContext.Impl.class, deserializationContext);

		// Retrieve deserializers and serializers iterators.
		Iterator<Deserializers> deserializersIterator = ((BeanDeserializerFactory) factory)
			.getFactoryConfig().deserializers().iterator();
		Iterator<Serializers> serializersIterator = ((BeanSerializerFactory) mapper.getSerializerFactory())
			.getFactoryConfig().serializers().iterator();

		// Ensure the deserializers and serializers are of the expected types.
		assertTrue(deserializersIterator.hasNext(), "Expected at least one deserializer.");
		assertTrue(serializersIterator.hasNext(), "Expected at least one serializer.");
		assertInstanceOf(SimpleDeserializers.class, deserializersIterator.next());
		assertInstanceOf(SimpleSerializers.class, serializersIterator.next());

		// Check that the serializer factory is correctly set.
		SerializerFactory serializerFactory = mapper.getSerializerFactory();
		assertInstanceOf(BeanSerializerFactory.class, serializerFactory);

		// Ensure the Jackson modules are properly registered.
		assertEquals(4, mapper.getRegisteredModuleIds().size(), "Expected 4 registered Jackson modules.");
	}
}

