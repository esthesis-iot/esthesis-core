package esthesis.dataflows.oriongateway.service;

import static java.util.function.Predicate.not;

import esthesis.common.data.DataUtils.ValueType;
import esthesis.common.exception.QDoesNotExistException;
import esthesis.dataflows.oriongateway.client.OrionClient;
import esthesis.dataflows.oriongateway.client.OrionClientHeaderFilter;
import esthesis.dataflows.oriongateway.client.OrionKeyrockAuthClient;
import esthesis.dataflows.oriongateway.config.AppConfig;
import esthesis.dataflows.oriongateway.dto.OrionAttributeDTO;
import esthesis.dataflows.oriongateway.dto.OrionEntityDTO;
import esthesis.dataflows.oriongateway.dto.OrionMetadataDTO;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

	static final String VALUE_ATR = "value";
	static final String TYPE_ATR = "type";

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
			String keyrockUrl = this.appConfig.orionAuthenticationUrl().orElseThrow().trim();

			OrionKeyrockAuthClient keyrockAuthClient = createKeyRockAuthClient(keyrockUrl);

			authService = new OrionKeyrockAuthService(this.appConfig, keyrockAuthClient);
		} else {
			authService = new OrionNoAuthService();
		}

		orionClient = createOrionClient(contexts, contextsRelationships, orionUrl);
	}

	OrionKeyrockAuthClient createKeyRockAuthClient(String keyrockUrl) {
		return RestClientBuilder.newBuilder()
			.baseUri(URI.create(keyrockUrl))
			.build(OrionKeyrockAuthClient.class);
	}

	OrionClient createOrionClient(List<String> contexts, List<String> contextsRelationships, URI orionUrl) {
		return RestClientBuilder.newBuilder()
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
	private JsonObject toOrionAttributeJson(Object attributeValue, ValueType attributeValueType,
																					ATTRIBUTE_TYPE attributeType) {

		String attributeValueParsed = String.valueOf(attributeValue);

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
				case BOOLEAN -> builder.add(VALUE_ATR, Boolean.parseBoolean(attributeValueParsed));
				case BYTE -> builder.add(VALUE_ATR, Byte.parseByte(attributeValueParsed));
				case SHORT -> builder.add(VALUE_ATR, Short.parseShort(attributeValueParsed));
				case INTEGER -> builder.add(VALUE_ATR, Integer.parseInt(attributeValueParsed));
				case LONG -> builder.add(VALUE_ATR, Long.parseLong(attributeValueParsed));
				case BIG_INTEGER -> builder.add(VALUE_ATR, new BigInteger(attributeValueParsed));
				case FLOAT, DOUBLE -> builder.add(VALUE_ATR, Double.parseDouble(attributeValueParsed));
				case BIG_DECIMAL -> builder.add(VALUE_ATR, new BigDecimal(attributeValueParsed));
				default -> builder.add(VALUE_ATR, attributeValueParsed);
			}
		} catch (Exception e) {
			log.warn("Failed to parse attribute value '{}' as type '{}'. Will default to a string "
				+ "representation.", attributeValueParsed, attributeValueType);
			builder.add(VALUE_ATR, attributeValueParsed);
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
		orionEntityDTO.getAttributes().forEach((attributeName, attributeDTO) -> {
			Object attributeValue = attributeDTO.getValue();
			ValueType attributeValueType = attributeDTO.getType();

			jsonBuilder.add(attributeName, toOrionAttributeJson(attributeValue, attributeValueType, ATTRIBUTE_TYPE.ATTRIBUTE));
		});

		String json = jsonBuilder.build().toString();
		log.debug("Creating entity in Orion {}", json);

		orionClient.createEntity(json);
	}

	/**
	 * Retrieves an entity from Orion by its ID and converts it into an {@link OrionEntityDTO}.
	 * Supports both NGSI-V2 and NGSI-LD, extracting values and metadata when available.
	 * Attributes are dynamically processed, handling structured values correctly.
	 *
	 * @param orionId The ID of the entity to retrieve.
	 * @return The entity as an {@link OrionEntityDTO}.
	 */

	public OrionEntityDTO getEntityByOrionId(String orionId) {
		try {
			log.debug("Get entity by Orion ID: {}", orionId);
			Map<String, Object> entity = orionClient.getEntity(orionId);

			if (Objects.nonNull(entity)) {
				OrionEntityDTO orionEntityDTO = new OrionEntityDTO();
				orionEntityDTO.setId(entity.get("id").toString());
				orionEntityDTO.setType(entity.get("type").toString());

				// Process attributes correctly
				Map<String, OrionAttributeDTO> attributesMap = new HashMap<>();
				entity.forEach((key, value) -> {
					if (!key.equals("id") && !key.equals("type")) {
						OrionAttributeDTO orionAttributeDTO = new OrionAttributeDTO();
						extractAttributeValue(value, orionAttributeDTO);
						attributesMap.put(key, orionAttributeDTO);
					}
				});

				orionEntityDTO.setAttributes(attributesMap);
				return orionEntityDTO;
			} else {
				log.debug("Entity with Orion ID: {} is null", orionId);
				return null;
			}
		} catch (NotFoundException | QDoesNotExistException e) {
			log.debug("Entity with Orion ID: {} was not found", orionId);
			return null;
		}
	}

	/**
	 * Extracts the value and type of attribute from an NGSI-V2 response.
	 */
	private void extractAttributeValue(Object value, OrionAttributeDTO orionAttributeDTO) {
		if (value instanceof Map<?, ?> valueMap) {

			// Extract "value" if it's inside a Map (NGSI-V2).
			Object extractedValue = valueMap.get(VALUE_ATR);
			orionAttributeDTO.setValue(extractedValue != null ? extractedValue : value);

			// Extract attribute type.
			Object extractedType = valueMap.get(TYPE_ATR);
			orionAttributeDTO.setType(extractedType != null ? ValueType.valueOf(extractedType.toString()) : null);

			// Extract metadata if present (NGSI-V2).
			Map<String, OrionMetadataDTO> metadata = extractMetadata(valueMap.get("metadata"));
			orionAttributeDTO.setMetadata(metadata.isEmpty() ? null : metadata);
		} else {
			orionAttributeDTO.setValue(value);
		}
	}

	/**
	 * Extracts metadata from an NGSI-V2 response if present.
	 */
	private Map<String, OrionMetadataDTO> extractMetadata(Object metadataObj) {
		if (!(metadataObj instanceof Map<?, ?> metadataMap)) {
			return Collections.emptyMap();
		}

		return metadataMap.entrySet().stream()
			.collect(Collectors.toMap(
				entry -> entry.getKey().toString(),
				entry -> {
					Map<?, ?> metaEntry = (Map<?, ?>) entry.getValue();
					return new OrionMetadataDTO(metaEntry.get(VALUE_ATR), (String) metaEntry.get(TYPE_ATR));
				}
			));
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
