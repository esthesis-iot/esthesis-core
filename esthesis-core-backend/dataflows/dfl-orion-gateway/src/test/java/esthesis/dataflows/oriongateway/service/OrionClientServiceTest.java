package esthesis.dataflows.oriongateway.service;

import esthesis.common.data.DataUtils.ValueType;
import esthesis.dataflows.oriongateway.TestHelper;
import esthesis.dataflows.oriongateway.client.OrionClient;
import esthesis.dataflows.oriongateway.client.OrionKeyrockAuthClient;
import esthesis.dataflows.oriongateway.config.AppConfig;
import esthesis.dataflows.oriongateway.dto.OrionEntityDTO;
import esthesis.dataflows.oriongateway.service.OrionClientService.ATTRIBUTE_TYPE;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.ws.rs.NotFoundException;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrionClientServiceTest {

	@Mock
	OrionClient orionClient;

	@Mock
	AppConfig appConfig;

	@Spy
	@InjectMocks
	OrionClientService orionClientService;

	TestHelper testHelper = new TestHelper();

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

	}

	@Test
	void initWithNoAuth() {
		// Mock rest clients creation.
		doReturn(orionClient).when(orionClientService).createOrionClient(anyList(), anyList(), any(URI.class));

		// Mock auth configurations for no authentication.
		when(appConfig.orionAuthenticationType()).thenReturn("NONE");
		when(appConfig.orionUrl()).thenReturn("http://test-orion-url");
		when(appConfig.orionLdDefinedContextsUrl()).thenReturn("https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld");
		when(appConfig.orionLdDefinedContextsRelationships()).thenReturn("https://www.w3.org/ns/json-ld#context");

		// Perform the init method and assert that it does not throw any exception.
		assertDoesNotThrow(() -> orionClientService.init());

		// Assert that the orionClient and authService are not null.
		assertNotNull(orionClientService.orionClient);
		assertNotNull(orionClientService.authService);

		// Assert that the orionClient was created correctly.
		verify(orionClientService, never()).createKeyRockAuthClient(anyString());
		verify(orionClientService).createOrionClient(anyList(), anyList(), any(URI.class));

		// Verify auth service was set correctly.
		assertInstanceOf(OrionNoAuthService.class, orionClientService.authService);
	}

	@Test
	void initWithKeyrockAuth() {
		// Mock rest clients creation.
		doReturn(orionClient).when(orionClientService).createOrionClient(anyList(), anyList(), any(URI.class));
		doReturn(mock(OrionKeyrockAuthClient.class)).when(orionClientService).createKeyRockAuthClient(anyString());

		// Mock auth configurations for Keyrock.
		when(appConfig.orionAuthenticationType()).thenReturn("KEYROCK");
		when(appConfig.orionAuthenticationUrl()).thenReturn(Optional.of("http://test-orion-auth-url"));
		when(appConfig.orionUrl()).thenReturn("http://test-orion-url");
		when(appConfig.orionLdDefinedContextsUrl()).thenReturn("https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld");
		when(appConfig.orionLdDefinedContextsRelationships()).thenReturn("https://www.w3.org/ns/json-ld#context");


		// Perform the init method and assert that it does not throw any exception.
		assertDoesNotThrow(() -> orionClientService.init());

		// Assert that the orionClient and authService are not null.
		assertNotNull(orionClientService.orionClient);
		assertNotNull(orionClientService.authService);

		// Assert that the orionClient was created correctly.
		verify(orionClientService).createKeyRockAuthClient("http://test-orion-auth-url");
		verify(orionClientService).createOrionClient(anyList(), anyList(), any(URI.class));

		// Verify auth service was set correctly.
		assertInstanceOf(OrionKeyrockAuthService.class, orionClientService.authService);
	}

	@ParameterizedTest
	@MethodSource("setAttributeTestCases")
	void setAttribute(String attributeName, String attributeValue, ValueType valueType, String expectedValue) {
		// Mock orion client request.
		doNothing().when(orionClient).appendAttributes(anyString(), anyString());

		// Mock default configurations.
		when(appConfig.esthesisOrionMetadataName()).thenReturn("maintainedBy");
		when(appConfig.esthesisAttributeSourceMetadataName()).thenReturn("attributeSource");
		when(appConfig.esthesisOrionMetadataValue()).thenReturn("esthesis");

		// Call the method to set the attribute.
		orionClientService.setAttribute(
			"test-entity-id",
			attributeName,
			attributeValue,
			valueType,
			ATTRIBUTE_TYPE.ATTRIBUTE);

		// Verify that the appendAttributes method was called with the correct parameters.
		String expectedJson = "{\"" + attributeName + "\":{\"maintainedBy\":{\"value\":\"esthesis\",\"type\":\"Property\"}," +
			"\"attributeSource\":{\"value\":\"ATTRIBUTE\",\"type\":\"Property\"},\"value\":" + expectedValue + "}}";

		verify(orionClient).appendAttributes("test-entity-id", expectedJson);
	}

	/**
	 * Test cases for the setAttribute method.
	 * Each test case consists of the attribute name, value, value type, and expected JSON value.
	 */
	private static Stream<Arguments> setAttributeTestCases() {
		return Stream.of(
			Arguments.of("test-attribute-string", "test-attribute-value", ValueType.STRING, "\"test-attribute-value\""),
			Arguments.of("test-attribute-big-decimal", "10.00", ValueType.BIG_DECIMAL, "10.00"),
			Arguments.of("test-attribute-big-integer", "100", ValueType.BIG_INTEGER, "100"),
			Arguments.of("test-attribute-integer", "100", ValueType.INTEGER, "100"),
			Arguments.of("test-attribute-boolean", "true", ValueType.BOOLEAN, "true"),
			Arguments.of("test-attribute-byte", "test", ValueType.BYTE, "\"test\""),
			Arguments.of("test-attribute-double", "10.00", ValueType.DOUBLE, "10.0"),
			Arguments.of("test-attribute-float", "10.00", ValueType.FLOAT, "10.0"),
			Arguments.of("test-attribute-long", "10", ValueType.LONG, "10"),
			Arguments.of("test-attribute-short", "10", ValueType.SHORT, "10")
		);
	}


	@Test
	void setAttributeCatchError() {
		// Mock orion client request.
		doNothing().when(orionClient).appendAttributes(anyString(), anyString());

		// Mock default configurations.
		when(appConfig.esthesisOrionMetadataName()).thenReturn("maintainedBy");
		when(appConfig.esthesisAttributeSourceMetadataName()).thenReturn("attributeSource");
		when(appConfig.esthesisOrionMetadataValue()).thenReturn("esthesis");

		// Call the method to set the attribute with an invalid attribute value for the given type.
		orionClientService.setAttribute(
			"test-entity-id",
			"test-attribute-name",
			"test-attribute-value",
			ValueType.INTEGER,
			ATTRIBUTE_TYPE.ATTRIBUTE);

		// Verify that the appendAttributes method was called with the correct parameters.
		String expectedJson = "{\"test-attribute-name\":{\"maintainedBy\":{\"value\":\"esthesis\",\"type\":\"Property\"}," +
			"\"attributeSource\":{\"value\":\"ATTRIBUTE\",\"type\":\"Property\"},\"value\":\"test-attribute-value\"}}";

		verify(orionClient).appendAttributes("test-entity-id", expectedJson);
	}


	@Test
	void saveOrUpdateEntities() {
		// Mock orion client request.
		doNothing().when(orionClient).createOrUpdateEntities(anyString());

		// Prepare a JSON entity.
		String entityJson = "{\"id\":\"test-entity-id\",\"type\":\"test-entity-type\"}";

		// Call the method to save or update the entity.
		orionClientService.saveOrUpdateEntities(entityJson);

		// Verify that the createOrUpdateEntities method was called with the correct JSON value.
		verify(orionClient).createOrUpdateEntities("[" + entityJson + "]");
	}

	@Test
	void deleteAttribute() {
		// Mock orion client request.
		doNothing().when(orionClient).deleteAttribute(anyString(), anyString());


		// Perform the delete attribute method.
		orionClientService.deleteAttribute("test-entity-id", "test-attribute-name");

		// Verify that the deleteAttribute method was called with the correct parameters.
		verify(orionClient).deleteAttribute("test-entity-id", "test-attribute-name");
	}

	@Test
	void createEntity() {
		// Mock default configurations.
		when(appConfig.esthesisOrionMetadataName()).thenReturn("maintainedBy");
		when(appConfig.esthesisAttributeSourceMetadataName()).thenReturn("attributeSource");
		when(appConfig.esthesisOrionMetadataValue()).thenReturn("esthesis");

		// Perform the operation to create an entity.
		orionClientService.createEntity(testHelper.createOrionEntity("test-orion-id"));

		// Assert that the createOrUpdateEntities method was called with the correct JSON value.
		verify(orionClient).createEntity("{\"id\":\"test-orion-id\",\"type\":\"test-type\",\"test-attribute\":" +
			"{\"maintainedBy\":{\"value\":\"esthesis\",\"type\":\"Property\"},\"attributeSource\":{\"value\":\"ATTRIBUTE\"," +
			"\"type\":\"Property\"},\"value\":\"test-value\"}}");
	}

	@Test
	void getEntityByOrionId() {
		// Create the expected Orion entity DTO.
		OrionEntityDTO orionEntity = testHelper.createOrionEntity("test-orion-id");

		// Mock the Orion entity map response.
		Map<String, Object> orionEntityMap = testHelper.createOrionEntityMap(orionEntity);
		when(orionClient.getEntity("test-orion-id")).thenReturn(orionEntityMap);


		// Call the service method.
		OrionEntityDTO resultedOrionEntity = orionClientService.getEntityByOrionId("test-orion-id");

		// Assert that the result is the same as the expected entity.
		assertEquals(orionEntity.getId(), resultedOrionEntity.getId());
		assertEquals(orionEntity.getType(), resultedOrionEntity.getType());
		assertEquals(orionEntity.getAttributes(), resultedOrionEntity.getAttributes());
	}

	@Test
	void getEntityByOrionIdNull() {
		// Mock the Orion entity map response as null.
		when(orionClient.getEntity("test-orion-id")).thenReturn(null);

		// Assert that the result is null.
		assertNull(orionClientService.getEntityByOrionId("test-orion-id"));
	}

	@Test
	void getEntityByOrionIdCatchNotFound() {
		// Mock the Orion entity map response to throw an NotFoundException.
		when(orionClient.getEntity("test-orion-id")).thenThrow(new NotFoundException());

		// Assert that the result is null.
		assertNull(orionClientService.getEntityByOrionId("test-orion-id"));
	}

	@Test
	void getVersion() {
		when(orionClient.getVersion()).thenReturn("test-version");
		assertEquals("test-version", orionClientService.getVersion());
	}

	@Test
	void deleteEntity() {
		// Mock orion client request.
		doNothing().when(orionClient).deleteEntity(anyString());

		orionClientService.deleteEntity("test-orion-id");
		verify(orionClient).deleteEntity("test-orion-id");
	}
}
