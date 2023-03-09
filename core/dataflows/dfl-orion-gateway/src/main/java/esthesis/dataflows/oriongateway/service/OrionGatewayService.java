package esthesis.dataflows.oriongateway.service;

import esthesis.avro.EsthesisDataMessage;
import esthesis.common.AppConstants.Device.Status;
import esthesis.common.data.ValueUtils.ValueType;
import esthesis.common.exception.QDoesNotExistException;
import esthesis.dataflows.oriongateway.config.AppConfig;
import esthesis.dataflows.oriongateway.dto.OrionAttributeDTO;
import esthesis.dataflows.oriongateway.dto.OrionEntityDTO;
import esthesis.service.device.entity.DeviceAttributeEntity;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.device.resource.DeviceSystemResource;
import io.quarkus.cache.CacheResult;
import java.util.List;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@ApplicationScoped
public class OrionGatewayService {

  @Inject
  AppConfig appConfig;

  @Inject
  @RestClient
  DeviceSystemResource deviceSystemResource;

  @Inject
  OrionClientService orionClientService;

  /**
   * Checks whether the device attributes allow registration.
   *
   * @param esthesisDeviceAttributes
   * @return
   */
  private boolean isRegistrationAllowed(List<DeviceAttributeEntity> esthesisDeviceAttributes) {
    if (appConfig.orionRegistrationEnabledAttribute().isEmpty()) {
      log.debug("No registration enabled attribute configured, so registration will proceed.");
      return true;
    } else {
      log.debug("Registration enabled attribute configured, so checking whether registration "
          + "should proceed.");
      Optional<String> orionRegistrationEnabledAttribute = appConfig.orionRegistrationEnabledAttribute();
      boolean shouldRegistrationProceed =
          orionRegistrationEnabledAttribute.map(s -> esthesisDeviceAttributes.stream()
                  .filter(attribute -> attribute.getAttributeName().equals(s))
                  .anyMatch(attribute -> attribute.getAttributeValue().equalsIgnoreCase("true")))
              .orElse(true);
      if (shouldRegistrationProceed) {
        log.debug("Registration enabled attribute is set to true, so registration will proceed.");
        return true;
      } else {
        log.debug(
            "Registration enabled attribute is set to false, so registration will not proceed.");
        return false;
      }
    }
  }

  /**
   * Returns whether a/ the service is configured to perform data updates (i.e. update the metrics
   * received by esthesis in Orion), b/ the specific device is already registered in Orion, and c/
   * the specific device has an attribute allowing/disallowing data updates.
   * <p>
   * The result of this method remains cached for a short period of time (configured in application
   * properties via the XXXXXXXXXX). This is to avoid unnecessary calls to Orion for every single
   * piece of data received (which can be multiple per second).
   *
   * @param esthesisHardwareId The esthesis hardware ID of the device.
   */
  @CacheResult(cacheName = "is-data-update-allowed")
  boolean isDataUpdateAllowed(String esthesisHardwareId) {
    System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
    // Check if the service is configured to perform data updates.
    if (!appConfig.orionUpdateData()) {
      log.trace("Data updates are disabled for this service.");
      return false;
    }

    // Check whether this device is registered in Orion.
    String orionId = orionClientService.getOrionIdByEsthesisHardwareId(esthesisHardwareId);
    if (orionId == null) {
      log.trace("Device with esthesis hardware ID '{}' is not registered in Orion.",
          esthesisHardwareId);
      return false;
    }

    // Check whether this device allows data updates based on device attributes.
    if (appConfig.orionUpdateDataAttribute().isEmpty()) {
      log.trace("No attribute configured to allow or prevent data updates, so data update will "
          + "proceed.");
      return true;
    }

    List<DeviceAttributeEntity> esthesisDeviceAttributes =
        deviceSystemResource.getDeviceAttributesByEsthesisHardwareId(esthesisHardwareId);
    @SuppressWarnings("java:S3655")
    String orionUpdateDataAttribute = appConfig.orionUpdateDataAttribute().get();
    boolean isUpdateAllowedByAttribute = esthesisDeviceAttributes.stream()
        .filter(attribute -> attribute.getAttributeName().equals(orionUpdateDataAttribute))
        .anyMatch(attribute -> attribute.getAttributeValue().equalsIgnoreCase("true"));
    if (isUpdateAllowedByAttribute) {
      log.trace("Device with esthesis hardware ID '{}' has attribute '{}' allowing data updates, "
          + "so data update will proceed.", esthesisHardwareId, orionUpdateDataAttribute);
      return true;
    } else {
      log.trace("Device with esthesis hardware ID '{}' has attribute '{}' preventing data updates, "
          + "so data update will not proceed.", esthesisHardwareId, orionUpdateDataAttribute);
      return false;
    }
  }

  /**
   * Generates the orion ID that an esthesis device should use. The resulting ID is based on the
   * configuration options available in the device attributes as well as in AppConfig.
   *
   * @param deviceId         The ID to use if no configuration options specify otherwise.
   * @param deviceAttributes The esthesis device attributes.
   */
  private String generateOrionDeviceId(String deviceId,
      List<DeviceAttributeEntity> deviceAttributes) {
    if (appConfig.orionIdAttribute().isEmpty()) {
      return appConfig.orionIdPrefix().orElse("") + deviceId;
    } else {
      String idAttribute = appConfig.orionIdAttribute().get();
      return deviceAttributes.stream()
          .filter(attribute -> attribute.getAttributeName().equals(idAttribute))
          .map(DeviceAttributeEntity::getAttributeValue).findFirst()
          .orElse(appConfig.orionIdPrefix().orElse("") + deviceId);
    }
  }

  /**
   * Generates the orion type that an esthesis device should use. The resulting type is based on the
   * configuration options available in the device attributes as well as in AppConfig.
   *
   * @param deviceAttributes The esthesis device attributes.
   */
  private String generateOrionDeviceType(List<DeviceAttributeEntity> deviceAttributes) {
    if (appConfig.orionTypeAttribute().isEmpty()) {
      return appConfig.orionDefaultType();
    } else {
      String typeAttribute = appConfig.orionTypeAttribute().get();
      return deviceAttributes.stream()
          .filter(attribute -> attribute.getAttributeName().equals(typeAttribute))
          .map(DeviceAttributeEntity::getAttributeValue).findFirst()
          .orElse(appConfig.orionDefaultType());
    }
  }

  /**
   * Scans all existing esthesis devices and tries to register them in Orion.
   */
  public void addExistingEsthesisDevicesToOrion() {
    log.debug("Adding existing esthesis devices to Orion.");
    deviceSystemResource.getDeviceIds().forEach(this::registerDeviceOnOrion);
  }

  /**
   * Orion device registration handler.
   *
   * @param esthesisId The esthesis ID of the device to register.
   */
  public void registerDeviceOnOrion(String esthesisId) {
    log.debug("Attempting to register device with esthesis ID '{}'.", esthesisId);

    // *********************************************************************************************
    // Pre-registration checks
    // *********************************************************************************************
    // Find the esthesis attributes of this device.
    List<DeviceAttributeEntity> esthesisDeviceAttributes = deviceSystemResource.getDeviceAttributesByEsthesisId(
        esthesisId);

    // Check whether this device should be registered.
    if (!isRegistrationAllowed(esthesisDeviceAttributes)) {
      log.debug("The attributes specified for device with esthesis ID '{}' do not allow "
          + "registration.", esthesisId);
      return;
    }

    // Find the esthesis device.
    DeviceEntity esthesisDevice = deviceSystemResource.findById(esthesisId);
    if (esthesisDevice == null) {
      log.debug("Device with esthesis ID '{}' not found in esthesis, skipping registration.",
          esthesisId);
      return;
    }
    if (esthesisDevice.getStatus() != Status.REGISTERED) {
      log.debug("Device has status '{}', only devices with status '{}' can be registered in Orion. "
          + "Registration skipped.", esthesisDevice.getStatus(), Status.REGISTERED);
      return;
    }

    // Check if this device is already registered in Orion.
    if (orionClientService.getEntityByEsthesisId(esthesisId) != null) {
      log.debug("Device with esthesis ID '{}' is already registered in Orion, skipping "
          + "registration.", esthesisDevice.getId());
      return;
    }

    // *********************************************************************************************
    // Registration
    // *********************************************************************************************
    // Find the Type and Orion ID to use for this device.
    String orionDeviceType = generateOrionDeviceType(esthesisDeviceAttributes);
    String orionDeviceId = generateOrionDeviceId(esthesisDevice.getHardwareId(),
        esthesisDeviceAttributes);

    // Create the Orion DTO to register the device with in Orion.
    OrionEntityDTO orionEntity = new OrionEntityDTO();
    orionEntity.setId(orionDeviceId);
    orionEntity.setType(orionDeviceType);
    // Add a custom attribute with the esthesis ID.
    esthesisDeviceAttributes.add(
        DeviceAttributeEntity.builder().attributeName(appConfig.metadataEsthesisId())
            .attributeValue(esthesisDevice.getId().toHexString()).attributeType(ValueType.STRING)
            .build());
    // Add a custom attribute with the esthesis hardware ID.
    esthesisDeviceAttributes.add(
        DeviceAttributeEntity.builder().attributeName(appConfig.metadataEsthesisHardwareId())
            .attributeValue(esthesisDevice.getHardwareId()).attributeType(ValueType.STRING)
            .build());
    orionEntity.setAttributes(
        OrionEntityDTO.attributesFromEsthesisDeviceAttributes(esthesisDeviceAttributes));

    orionClientService.createEntity(orionEntity);
    log.info("Device '{}' of type '{}' successfully registered in Orion.", orionDeviceId,
        orionDeviceType);
  }

  /**
   * Synchronises esthesis device attributes with Orion. The source of synchronisation is esthesis
   * and the target is Orion.
   */
  public void syncAttributes(String esthesisId) {
    log.debug("Synchronising attributes for device with esthesis ID '{}'.", esthesisId);
    // Get esthesis device attributes.
    List<DeviceAttributeEntity> esthesisAttributes = deviceSystemResource.getDeviceAttributesByEsthesisId(
        esthesisId);

    // Get Orion attributes.
    OrionEntityDTO orionEntity = orionClientService.getEntityByEsthesisId(esthesisId);

    // Find all Orion attributes managed by esthesis.
    List<DeviceAttributeEntity> esthesisManagedAttributes = esthesisAttributes.stream().filter(
        deviceAttribute -> !deviceAttribute.getAttributeName()
            .equals(appConfig.metadataEsthesisId())).toList();

    // Add all esthesis attributes in Orion.
    for (DeviceAttributeEntity esthesisAttribute : esthesisManagedAttributes) {
      log.debug("Setting attribute '{}' to value '{}' in Orion.",
          esthesisAttribute.getAttributeName(), esthesisAttribute.getAttributeValue());
      orionClientService.setAttribute(orionEntity.getId(), esthesisAttribute.getAttributeName(),
          esthesisAttribute.getAttributeValue(), esthesisAttribute.getAttributeType());
    }

    // For every esthesis managed attribute in Orion, if it does not exist in esthesis, delete it.
    for (OrionAttributeDTO orionAttribute : orionEntity.getAttributes()) {
      if (!orionAttribute.getName().equals(appConfig.metadataEsthesisId())
          && esthesisManagedAttributes.stream()
          .noneMatch(attribute -> attribute.getAttributeName().equals(orionAttribute.getName()))) {
        log.debug("Deleting attribute '{}' from Orion.", orionAttribute.getName());
        // Delete the attribute.
        orionClientService.deleteAttribute(orionEntity.getId(), orionAttribute.getName());
      }
    }
  }

  public void deleteEntityByEsthesisId(String esthesisId) {
    // Find the device in Orion.
    OrionEntityDTO orionEntity = orionClientService.getEntityByEsthesisId(esthesisId);
    if (orionEntity == null) {
      log.warn("Device with esthesis ID '{}' not found in Orion, skipping deletion.", esthesisId);
      return;
    }

    // Delete Entity in Orion.
    log.debug("Attempting to delete device with Orion ID '{}' from Orion.", orionEntity.getId());
    try {
      orionClientService.deleteEntity(orionEntity.getId());
      log.info("Device with Orion ID '{}' deleted from Orion.", orionEntity.getId());
    } catch (QDoesNotExistException e) {
      log.info("Device with Orion ID '{}' not found in Orion, skipping deletion.",
          orionEntity.getId());
    }
  }

  public void processData(Exchange exchange) {
    // Get the message from the exchange.
    EsthesisDataMessage esthesisMessage = exchange.getIn().getBody(EsthesisDataMessage.class);
    log.debug("Processing '{}'.", esthesisMessage);

    // Extract the esthesis hardware ID from the message.
    String esthesisHardwareId = esthesisMessage.getHardwareId();

    // Check if this device's data should be updated in Orion.
    if (isDataUpdateAllowed(esthesisHardwareId)) {
      String category = esthesisMessage.getPayload().getCategory();
      esthesisMessage.getPayload().getValues().forEach((valueData) -> {
        orionClientService.setAttribute(esthesisHardwareId, category + "." + valueData.getName(),
            valueData.getValue(), ValueType.valueOf(valueData.getValueType().name()));
      });
    }
  }
}
