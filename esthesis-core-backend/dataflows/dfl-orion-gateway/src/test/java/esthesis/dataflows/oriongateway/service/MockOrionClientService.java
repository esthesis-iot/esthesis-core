package esthesis.dataflows.oriongateway.service;

import esthesis.common.data.DataUtils;
import esthesis.dataflows.oriongateway.client.OrionClient;
import esthesis.dataflows.oriongateway.client.OrionKeyrockAuthClient;
import esthesis.dataflows.oriongateway.dto.OrionEntityDTO;
import io.quarkus.test.Mock;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.List;

/**
 * Mock implementation of the OrionClientService in order to
 * work around the fact that the OrionClientService has a @PostConstruct method,
 * which causes issues during testing.
 * This class overrides methods to provide mock behavior and logging.
 */

@Slf4j
@Mock
@ApplicationScoped
public class MockOrionClientService extends OrionClientService{

	@Override
	void init() {
		log.info("Mock OrionClientService init.");
	}

	@Override
	OrionKeyrockAuthClient createKeyRockAuthClient(String keyrockUrl) {
		return null;
	}

	@Override
	OrionClient createOrionClient(List<String> contexts, List<String> contextsRelationships, URI orionUrl) {
		return null;
	}

	@Override
	public void setAttribute(String entityId, String attributeName, String attributeValue, DataUtils.ValueType attributeValueType, ATTRIBUTE_TYPE attributeType) {
		log.info("Mock setting attribute {} to {} for entity {}", attributeName, attributeValue, entityId);
	}

	@Override
	public void saveOrUpdateEntities(String entitiesJson) {
		log.info("Mock saving/updating entities: {}", entitiesJson);
	}

	@Override
	public void deleteAttribute(String entityId, String attributeName) {
		log.info("Mock deletion of attribute {} from entity {}", attributeName, entityId);
	}

	@Override
	public void createEntity(OrionEntityDTO orionEntityDTO) {
		log.info("Mock creation of entity: {}", orionEntityDTO);
	}

	@Override
	public OrionEntityDTO getEntityByOrionId(String orionId) {
		return new OrionEntityDTO();
	}

	@Override
	public String getVersion() {
		return "test";
	}

	@Override
	public void deleteEntity(String orionId) {
		log.info("Mock deletion of entity with ID: {}", orionId);
	}
}
