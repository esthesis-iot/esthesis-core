package esthesis.dataflows.oriongateway.service;

import esthesis.common.data.ValueUtils.ValueType;
import esthesis.dataflows.oriongateway.client.OrionClient;
import esthesis.dataflows.oriongateway.config.AppConfig;
import esthesis.dataflows.oriongateway.dto.OrionAttributeDTO;
import esthesis.dataflows.oriongateway.dto.OrionEntityDTO;
import esthesis.dataflows.oriongateway.dto.OrionQueryDTO;
import esthesis.dataflows.oriongateway.dto.OrionQueryDTO.Expression;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

@Slf4j
@ApplicationScoped
public class OrionClientService {

	// The Orion client isn't automatically injected, as we need to configure its URL dynamically
	// at runtime.
	OrionClient orionClient;

	@Inject
	AppConfig appConfig;

	public enum ATTRIBUTE_TYPE {
		ATTRIBUTE, TELEMETRY, METADATA
	}

	@PostConstruct
	void init() {
		// Configure Orion client.
		log.info("Configuring Orion client for '{}'.", appConfig.orionUrl());
		URI orionUrl = URI.create(appConfig.orionUrl());
		orionClient = RestClientBuilder.newBuilder().baseUri(orionUrl).build(OrionClient.class);
	}

	private JsonObject toOrionAttributeJson(String attributeValue, ValueType attributeValueType,
		ATTRIBUTE_TYPE attributeType) {
		// Create metadata for this attribute.
		JsonObjectBuilder builder = Json.createObjectBuilder()
			.add("metadata",
				Json.createObjectBuilder()
					.add(appConfig.esthesisOrionMetadataName(),
						Json.createObjectBuilder()
							.add("value", appConfig.esthesisOrionMetadataValue())
							.add("type", "Text"))
					.add(appConfig.esthesisAttributeSourceMetadataName(),
						Json.createObjectBuilder()
							.add("value", attributeType.name())
							.add("type", "Text"))
			);

		// Set the value of the attribute.
		try {
			switch (attributeValueType) {
				case BOOLEAN -> builder.add("value", Boolean.parseBoolean(attributeValue));
				case BYTE -> builder.add("value", Byte.parseByte(attributeValue));
				case SHORT -> builder.add("value", Short.parseShort(attributeValue));
				case INTEGER -> builder.add("value", Integer.parseInt(attributeValue));
				case LONG -> builder.add("value", Long.parseLong(attributeValue));
				case BIG_INTEGER -> builder.add("value", new BigInteger(attributeValue));
				case FLOAT, DOUBLE -> builder.add("value", Double.parseDouble(attributeValue));
				case BIG_DECIMAL -> builder.add("value", new BigDecimal(attributeValue));
				default -> builder.add("value", attributeValue);
			}
		} catch (Exception e) {
			log.warn("Failed to parse attribute value '{}' as type '{}'. Will default to a string "
				+ "representation.", attributeValue, attributeValueType);
			builder.add("value", attributeValue);
		}

		return builder.build();
	}

	public void setAttribute(String entityId, String attributeName, String attributeValue,
		ValueType attributeValueType, ATTRIBUTE_TYPE attributeType) {
		JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
		jsonBuilder.add(attributeName,
			toOrionAttributeJson(attributeValue, attributeValueType, attributeType));
		orionClient.setAttribute(entityId, jsonBuilder.build().toString());
	}

	public void deleteAttribute(String entityId, String attributeName) {
		orionClient.deleteAttribute(entityId, attributeName);
	}

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

		orionClient.createEntity(jsonBuilder.build().toString());
	}

	/**
	 * Returns the Orion ID of the device with the given esthesis hardware ID.
	 *
	 * @param esthesisHardwareId The esthesis hardware ID of the device.
	 */
	public String getOrionIdByEsthesisHardwareId(String esthesisHardwareId) {
		List<Map<String, Object>> entitiesMatched = orionClient.query(
			OrionQueryDTO.builder().expression(
					Expression.builder()
						.q(appConfig.attributeEsthesisHardwareId() + "==" + esthesisHardwareId).build())
				.build());
		if (CollectionUtils.isEmpty(entitiesMatched)) {
			return null;
		} else {
			if (entitiesMatched.size() > 1) {
				log.warn(
					"Multiple devices found in Orion with esthesis hardware ID '{}'. Returning the first one.",
					esthesisHardwareId);
			}
			Map<String, Object> entity = entitiesMatched.get(0);
			return entity.get("id").toString();
		}
	}

	public OrionEntityDTO getEntityByEsthesisId(String esthesisId) {
		// Find the Orion Entity for this device.
		List<Map<String, Object>> entitiesMatched = orionClient.query(
			OrionQueryDTO.builder().expression(
					Expression.builder().q(appConfig.attributeEsthesisId() + "==" + esthesisId).build())
				.build());

		if (CollectionUtils.isEmpty(entitiesMatched)) {
			return null;
		} else {
			if (entitiesMatched.size() > 1) {
				log.warn("Multiple devices found in Orion with esthesis ID '{}'. Returning the first one.",
					esthesisId);
			}
			Map<String, Object> entity = entitiesMatched.get(0);
			OrionEntityDTO orionEntityDTO = new OrionEntityDTO();
			orionEntityDTO.setId(entity.get("id").toString());
			orionEntityDTO.setType(entity.get("type").toString());

			// Add remaining keys as attributes.
			entity.forEach((key, value) -> {
				if (!key.equals("id") && !key.equals("type")) {
					OrionAttributeDTO orionAttributeDTO = new OrionAttributeDTO();
					orionAttributeDTO.setName(key);
					orionAttributeDTO.setValue(value.toString());
					if (key.equals("metadata")) {
						value = ((Map<String, Object>) value).get("maintainedBy");
						if (value != null) {
							orionAttributeDTO.setMaintainedByEsthesis(
								Boolean.parseBoolean(((Map<String, Object>) value).get("value").toString()));
						}
					}
					orionEntityDTO.getAttributes().add(orionAttributeDTO);
				}
			});

			return orionEntityDTO;
		}
	}

	public String getVersion() {
		return orionClient.getVersion();
	}

	public void deleteEntity(String orionId) {
		orionClient.deleteEntity(orionId);
	}

}
