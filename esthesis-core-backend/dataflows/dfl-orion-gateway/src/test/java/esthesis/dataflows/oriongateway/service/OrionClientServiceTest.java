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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

		// Mock rest clients creation.
		doReturn(orionClient).when(orionClientService).createOrionClient(anyList(), anyList(), any(URI.class));
		doReturn(mock(OrionKeyrockAuthClient.class)).when(orionClientService).createKeyRockAuthClient(anyString());

		// Mock default configurations.
		when(appConfig.orionUrl()).thenReturn("http://test-orion-url");
		when(appConfig.orionLdDefinedContextsUrl()).thenReturn("https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld");
		when(appConfig.orionLdDefinedContextsRelationships()).thenReturn("https://www.w3.org/ns/json-ld#context");
		when(appConfig.esthesisOrionMetadataName()).thenReturn("maintainedBy");
		when(appConfig.esthesisAttributeSourceMetadataName()).thenReturn("attributeSource");
		when(appConfig.esthesisOrionMetadataValue()).thenReturn("esthesis");
		when(appConfig.orionAuthenticationType()).thenReturn("NONE");
		when(appConfig.orionAuthenticationUrl()).thenReturn(Optional.empty());
		when(appConfig.orionAuthenticationCredentialToken()).thenReturn(Optional.empty());
		when(appConfig.orionAuthenticationUsername()).thenReturn(Optional.empty());
		when(appConfig.orionAuthenticationPassword()).thenReturn(Optional.empty());
		when(appConfig.orionAuthenticationGrantType()).thenReturn(Optional.empty());
		when(appConfig.orionLdTenant()).thenReturn(Optional.empty());
		when(appConfig.orionCustomEntityJsonFormatAttributeName()).thenReturn(Optional.empty());
		when(appConfig.orionCustomEntityJsonFormat()).thenReturn(Optional.empty());
		when(appConfig.orionAttributesToSync()).thenReturn(Optional.empty());

		// Mock orion client requests.
		doNothing().when(orionClient).appendAttributes(anyString(), anyString());
		doNothing().when(orionClient).createOrUpdateEntities(anyString());
		doNothing().when(orionClient).deleteAttribute(anyString(), anyString());
		doNothing().when(orionClient).deleteEntity(anyString());

	}

	@Test
	void initWithNoAuth() {
		// Mock auth configurations for no authentication.
		when(appConfig.orionAuthenticationType()).thenReturn("NONE");

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
		// Mock auth configurations for Keyrock.
		when(appConfig.orionAuthenticationType()).thenReturn("KEYROCK");
		when(appConfig.orionAuthenticationUrl()).thenReturn(Optional.of("http://test-orion-auth-url"));


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

	@Test
	void setAttribute() {

		// Call the method to set the attribute.
		orionClientService.setAttribute(
			"test-entity-id",
			"test-attribute-name",
			"test-attribute-value",
			ValueType.STRING,
			ATTRIBUTE_TYPE.ATTRIBUTE);

		// Verify that the appendAttributes method was called with the correct parameters.
		String expectedJson = "{\"test-attribute-name\":{\"maintainedBy\":{\"value\":\"esthesis\",\"type\":\"Property\"}," +
			"\"attributeSource\":{\"value\":\"ATTRIBUTE\",\"type\":\"Property\"},\"value\":\"test-attribute-value\"}}";

		verify(orionClient).appendAttributes("test-entity-id", expectedJson);
	}

	@Test
	void saveOrUpdateEntities() {
		// Prepare a JSON entity.
		String entityJson = "{\"id\":\"test-entity-id\",\"type\":\"test-entity-type\"}";

		// Call the method to save or update the entity.
		orionClientService.saveOrUpdateEntities(entityJson);

		// Verify that the createOrUpdateEntities method was called with the correct JSON value.
		verify(orionClient).createOrUpdateEntities("[" + entityJson + "]");
	}

	@Test
	void deleteAttribute() {
		// Perform the delete attribute method.
		orionClientService.deleteAttribute("test-entity-id", "test-attribute-name");

		// Verify that the deleteAttribute method was called with the correct parameters.
		verify(orionClient).deleteAttribute("test-entity-id", "test-attribute-name");
	}

	@Test
	void createEntity() {
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
	void getVersion() {
		when(orionClient.getVersion()).thenReturn("test-version");
		assertEquals("test-version", orionClientService.getVersion());
	}

	@Test
	void deleteEntity() {
		orionClientService.deleteEntity("test-orion-id");
		verify(orionClient).deleteEntity("test-orion-id");
	}
}
