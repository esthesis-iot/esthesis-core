package esthesis.dataflows.oriongateway.util;

import esthesis.common.data.ValueUtils.ValueType;
import esthesis.dataflows.oriongateway.config.AppConfig;
import esthesis.service.device.entity.DeviceAttributeEntity;
import java.math.BigDecimal;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class OrionAttributesBuilder {

  @Inject
  AppConfig appConfig;

  /**
   * Creates a JSON object from the given attribute names and values.
   */
  public JsonObject build(List<DeviceAttributeEntity> attributes) {
    final String valueAttributeName = "value";
    final String metadataAttributeName = "metadata";

    // Create a JSON builder to build the JSON object.
    JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();

    // Add each attribute to the JSON object.
    attributes.forEach(entry -> {
      String attributeName = entry.getAttributeName();
      String attributeValue = entry.getAttributeValue();
      ValueType attributeValueType = entry.getAttributeType();

      JsonObject maintendByEsthesisMetadata = Json.createObjectBuilder()
          .add(appConfig.esthesisOrionMetadataName(),
              Json.createObjectBuilder()
                  .add(valueAttributeName, appConfig.esthesisOrionMetadataValue())
                  .add("type", "Text")
                  .build())
          .build();
      try {
        switch (attributeValueType) {
          case BOOLEAN -> jsonObjectBuilder.add(attributeName,
              Json.createObjectBuilder()
                  .add(valueAttributeName, Boolean.parseBoolean(attributeValue))
                  .add(metadataAttributeName, maintendByEsthesisMetadata)
                  .build());
          case BYTE -> jsonObjectBuilder.add(attributeName,
              Json.createObjectBuilder()
                  .add(valueAttributeName, Byte.parseByte(attributeValue))
                  .add(metadataAttributeName, maintendByEsthesisMetadata)
                  .build());
          case SHORT -> jsonObjectBuilder.add(attributeName,
              Json.createObjectBuilder()
                  .add(valueAttributeName, Short.parseShort(attributeValue))
                  .add(metadataAttributeName, maintendByEsthesisMetadata)
                  .build());
          case INTEGER -> jsonObjectBuilder.add(attributeName,
              Json.createObjectBuilder()
                  .add(valueAttributeName, Integer.parseInt(attributeValue))
                  .add(metadataAttributeName, maintendByEsthesisMetadata)
                  .build());
          case LONG -> jsonObjectBuilder.add(attributeName,
              Json.createObjectBuilder()
                  .add(valueAttributeName, Long.parseLong(attributeValue))
                  .add(metadataAttributeName, maintendByEsthesisMetadata)
                  .build());
          case BIG_DECIMAL -> jsonObjectBuilder.add(attributeName,
              Json.createObjectBuilder()
                  .add(valueAttributeName, new BigDecimal(attributeValue))
                  .add(metadataAttributeName, maintendByEsthesisMetadata)
                  .build());
          default -> jsonObjectBuilder.add(attributeName,
              Json.createObjectBuilder()
                  .add(valueAttributeName, attributeValue)
                  .add(metadataAttributeName, maintendByEsthesisMetadata)
                  .build());
        }
      } catch (Exception e) {
        log.warn("Failed to parse attribute value '{}' as type '{}'. Will default to a string "
            + "representation.", attributeValue, attributeValueType);
        jsonObjectBuilder.add(attributeName,
            Json.createObjectBuilder()
                .add(valueAttributeName, attributeValue)
                .add(metadataAttributeName, maintendByEsthesisMetadata)
                .build());
      }
    });

    return jsonObjectBuilder.build();
  }
}
