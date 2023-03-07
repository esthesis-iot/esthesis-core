package esthesis.dataflows.oriongateway.service;

import esthesis.common.data.ValueUtils.ValueType;
import esthesis.common.exception.QDoesNotExistException;
import esthesis.dataflows.oriongateway.client.OrionClient;
import esthesis.dataflows.oriongateway.config.AppConfig;
import esthesis.dataflows.oriongateway.dto.OrionAttributeDTO;
import esthesis.dataflows.oriongateway.dto.OrionEntityDTO;
import esthesis.dataflows.oriongateway.dto.OrionQueryDTO;
import esthesis.dataflows.oriongateway.dto.OrionQueryDTO.Expression;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
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

  @PostConstruct
  void init() {
    // Configure Orion client.
    log.info("Configuring Orion client for '{}'.", appConfig.orionUrl());
    URI orionUrl = URI.create(appConfig.orionUrl());
    orionClient = RestClientBuilder.newBuilder().baseUri(orionUrl).build(OrionClient.class);
  }

  private JsonObject toOrionAttributeJson(String attributeValue, ValueType attributeValueType) {
    JsonObject maintendByEsthesisMetadata = Json.createObjectBuilder()
        .add(appConfig.esthesisOrionMetadataName(),
            Json.createObjectBuilder()
                .add("value", appConfig.esthesisOrionMetadataValue())
                .add("type", "Text")
                .build())
        .build();
    try {
      switch (attributeValueType) {
        case BOOLEAN -> { return
            Json.createObjectBuilder()
                .add("value", Boolean.parseBoolean(attributeValue))
                .add("metadata", maintendByEsthesisMetadata)
                .build(); }
        case BYTE -> { return
            Json.createObjectBuilder()
                .add("value", Byte.parseByte(attributeValue))
                .add("metadata", maintendByEsthesisMetadata)
                .build(); }
        case SHORT -> { return
            Json.createObjectBuilder()
                .add("value", Short.parseShort(attributeValue))
                .add("metadata", maintendByEsthesisMetadata)
                .build(); }
        case INTEGER -> { return
            Json.createObjectBuilder()
                .add("value", Integer.parseInt(attributeValue))
                .add("metadata", maintendByEsthesisMetadata)
                .build();}
        case LONG -> { return
            Json.createObjectBuilder()
                .add("value", Long.parseLong(attributeValue))
                .add("metadata", maintendByEsthesisMetadata)
                .build(); }
        case BIGDECIMAL -> { return
            Json.createObjectBuilder()
                .add("value", new BigDecimal(attributeValue))
                .add("metadata", maintendByEsthesisMetadata)
                .build(); }
        default -> { return
            Json.createObjectBuilder()
                .add("value", attributeValue)
                .add("metadata", maintendByEsthesisMetadata)
                .build(); }
      }
    } catch (Exception e) {
      log.warn("Failed to parse attribute value '{}' as type '{}'. Will default to a string "
          + "representation.", attributeValue, attributeValueType);
      return
          Json.createObjectBuilder()
              .add("value", attributeValue)
              .add("metadata", maintendByEsthesisMetadata)
              .build();
    }
  }

  public void setAttribute(String entityId, String attributeName, String attributeValue,
      ValueType attributeValueType) {
    JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
    jsonBuilder.add(attributeName, toOrionAttributeJson(attributeValue, attributeValueType));
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
      jsonBuilder.add(attributeName, toOrionAttributeJson(attributeValue, attributeValueType));
    });

    orionClient.createEntity(jsonBuilder.build().toString());
  }

  public OrionEntityDTO getEntityByEsthesisId(String esthesisId) {
    // Find the Orion Entity for this device.
    List<Map<String, Object>> entitiesMatched = orionClient.query(
        OrionQueryDTO.builder().expression(
                Expression.builder().q(appConfig.metadataEsthesisId() + "==" + esthesisId).build())
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
