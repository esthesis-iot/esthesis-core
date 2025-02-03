package esthesis.dataflows.oriongateway.service;

import static java.util.function.Predicate.not;

import esthesis.common.data.DataUtils.ValueType;
import esthesis.common.exception.QDoesNotExistException;
import esthesis.dataflows.oriongateway.client.OrionClient;
import esthesis.dataflows.oriongateway.client.OrionClientHeaderFilter;
import esthesis.dataflows.oriongateway.config.AppConfig;
import esthesis.dataflows.oriongateway.dto.OrionAttributeDTO;
import esthesis.dataflows.oriongateway.dto.OrionEntityDTO;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

/**
 * Service to interact with Orion Context Broker.
 */
@Slf4j
@Transactional
@ApplicationScoped
public class OrionClientService {

	// The Orion client isn't automatically injected, as we need to configure its URL dynamically
	// at runtime.
	OrionClient orionClient;
	// Defined at runtime as it rely on the configurations set for auth
	OrionAuthService authService;
	@Inject
	AppConfig appConfig;

	public enum ATTRIBUTE_TYPE {
		ATTRIBUTE, TELEMETRY, METADATA
	}

	public enum AUTHENTICATION_TYPE {
		NONE, KEYROCK
	}

	@PostConstruct
	void init() {
		// Configure Orion client.
		log.info("Configuring Orion client for '{}'.", appConfig.orionUrl());
		URI orionUrl = URI.create(appConfig.orionUrl());

		// Get the LD contexts urls from configuration variable. It is necessary to create the Link header
		List<String> contexts = Arrays.stream(appConfig.orionLdDefinedContextsUrl().split(","))
			.map(String::trim)
			.filter(not(String::isBlank)).toList();

		// Get contexts relationship definitions from configuration variable. It is necessary to create the Link header
		List<String> contextsRelationships = Arrays.stream(
				appConfig.orionLdDefinedContextsRelationships().split(","))
			.map(String::trim)
			.filter(not(String::isBlank)).toList();

		// Check for auth configurations and set the auth service accordingly.
		// If no auth is defined then uses the OrionNoAuthService
		if (AUTHENTICATION_TYPE.KEYROCK.name().equalsIgnoreCase(appConfig.orionAuthenticationType())) {
			authService = new OrionKeyrockAuthService(this.appConfig);
		} else {
			authService = new OrionNoAuthService();
		}

		orionClient = RestClientBuilder.newBuilder()
			.register(new OrionClientHeaderFilter(contexts, contextsRelationships, authService,
				this.appConfig.orionLdTenant().orElse(null)))
			.baseUri(orionUrl).build(OrionClient.class);
	}

	/**
	 * Sets an attribute for an entity in Orion.
	 *
	 * @param entityId           The ID of the entity.
	 * @param attributeName      The name of the attribute.
	 * @param attributeValue     The value of the attribute.
	 * @param attributeValueType The type of the attribute value.
	 * @param attributeType      The type of the attribute.
	 */
	public void setAttribute(String entityId, String attributeName, String attributeValue,
		ValueType attributeValueType, ATTRIBUTE_TYPE attributeType) {
		JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
		jsonBuilder.add(attributeName,
			toOrionAttributeJson(attributeValue, attributeValueType, attributeType));
		String json = jsonBuilder.build().toString();
		log.debug("Sending attribute to orion '{}' for entity '{}'", json, entityId);
		orionClient.appendAttributes(entityId, json);
	}

	/**
	 * Converts an attribute value to a JSON object that can be sent to Orion.
	 *
	 * @param attributeValue     The value of the attribute.
	 * @param attributeValueType The type of the attribute value.
	 * @param attributeType      The type of the attribute.
	 * @return The JSON object representing the attribute.
	 */
	private JsonObject toOrionAttributeJson(String attributeValue, ValueType attributeValueType,
		ATTRIBUTE_TYPE attributeType) {
		final String VALUE_ATR = "value";
		final String TYPE_ATR = "type";

		// Create metadata for this attribute.
		JsonObjectBuilder builder = Json.createObjectBuilder()
			.add(appConfig.esthesisOrionMetadataName(),
				Json.createObjectBuilder()
					.add(VALUE_ATR, appConfig.esthesisOrionMetadataValue())
					.add(TYPE_ATR, "Property"))
			.add(appConfig.esthesisAttributeSourceMetadataName(),
				Json.createObjectBuilder()
					.add(VALUE_ATR, attributeType.name())
					.add(TYPE_ATR, "Property"));

		// Set the value of the attribute.
		try {
			switch (attributeValueType) {
				case BOOLEAN -> builder.add(VALUE_ATR, Boolean.parseBoolean(attributeValue));
				case BYTE -> builder.add(VALUE_ATR, Byte.parseByte(attributeValue));
				case SHORT -> builder.add(VALUE_ATR, Short.parseShort(attributeValue));
				case INTEGER -> builder.add(VALUE_ATR, Integer.parseInt(attributeValue));
				case LONG -> builder.add(VALUE_ATR, Long.parseLong(attributeValue));
				case BIG_INTEGER -> builder.add(VALUE_ATR, new BigInteger(attributeValue));
				case FLOAT, DOUBLE -> builder.add(VALUE_ATR, Double.parseDouble(attributeValue));
				case BIG_DECIMAL -> builder.add(VALUE_ATR, new BigDecimal(attributeValue));
				default -> builder.add(VALUE_ATR, attributeValue);
			}
		} catch (Exception e) {
			log.warn("Failed to parse attribute value '{}' as type '{}'. Will default to a string "
				+ "representation.", attributeValue, attributeValueType);
			builder.add(VALUE_ATR, attributeValue);
		}

		return builder.build();
	}

	/**
	 * Saves or updates a list of entities in Orion.
	 *
	 * @param entitiesJson The JSON representation of the entities.
	 */
	public void saveOrUpdateEntities(String entitiesJson) {
		//Check and add brackets in case it is missing in the custom entities JSON
		if (!entitiesJson.trim().startsWith("[")) {
			entitiesJson = "[" + entitiesJson + "]";
		}
		log.debug("Sending data to orion {}", entitiesJson);
		orionClient.createOrUpdateEntities(entitiesJson);
	}

	/**
	 * Deletes an attribute from an entity in Orion.
	 *
	 * @param entityId      The ID of the entity.
	 * @param attributeName The name of the attribute.
	 */
	public void deleteAttribute(String entityId, String attributeName) {
		orionClient.deleteAttribute(entityId, attributeName);
	}

	/**
	 * Creates an entity in Orion.
	 *
	 * @param orionEntityDTO The entity to create.
	 */
	public void createEntity(OrionEntityDTO orionEntityDTO) {
		// Create a JSON builder to build the JSON representation of the new Orion device.
		JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();

		// Add ID and Type.
		jsonBuilder.add("id", orionEntityDTO.getId());
		jsonBuilder.add("type", orionEntityDTO.getType());

		// Add attributes.
		orionEntityDTO.getAttributes().forEach(entry -> {
			String attributeName = entry.getName();
			String attributeValue = entry.getValue();
			ValueType attributeValueType = entry.getType();
			jsonBuilder.add(attributeName, toOrionAttributeJson(attributeValue, attributeValueType,
				ATTRIBUTE_TYPE.ATTRIBUTE));
		});

		String json = jsonBuilder.build().toString();
		log.debug("Creating entity in orion {}", json);

		orionClient.createEntity(json);
	}

	/**
	 * Retrieves an entity from Orion by its ID.
	 *
	 * @param orionId The entity to update.
	 */
	public OrionEntityDTO getEntityByOrionId(String orionId) {
		try {
			log.debug("Get entity by orion id: {}", orionId);
			Map<String, Object> entity = orionClient.getEntity(orionId);

			if (Objects.nonNull(entity)) {
				OrionEntityDTO orionEntityDTO = new OrionEntityDTO();
				orionEntityDTO.setId(entity.get("id").toString());
				orionEntityDTO.setType(entity.get("type").toString());

				// Add remaining keys as attributes.
				entity.forEach((key, value) -> {
					if (!key.equals("id") && !key.equals("type")) {
						OrionAttributeDTO orionAttributeDTO = new OrionAttributeDTO();
						orionAttributeDTO.setName(key);
						orionAttributeDTO.setValue(value.toString());
						orionEntityDTO.getAttributes().add(orionAttributeDTO);
					}
				});

				return orionEntityDTO;
			} else {
				log.debug("entity with orion id: {} is null", orionId);
				return null;
			}
		} catch (NotFoundException | QDoesNotExistException e) {
			log.debug("entity with orion id: {} was not found", orionId);
			// Return null if the entity is not found (404 error).
			return null;
		}

	}

	/**
	 * Retrieves the version of the Orion Context Broker.
	 *
	 * @return The version of the Orion Context Broker.
	 */
	public String getVersion() {
		return orionClient.getVersion();
	}

	/**
	 * Deletes an entity from Orion by its ID.
	 *
	 * @param orionId The ID of the entity to delete.
	 */
	public void deleteEntity(String orionId) {
		orionClient.deleteEntity(orionId);
	}

}
