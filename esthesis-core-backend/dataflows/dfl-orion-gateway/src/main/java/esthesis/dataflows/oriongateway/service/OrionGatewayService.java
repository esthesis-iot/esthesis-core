package esthesis.dataflows.oriongateway.service;

import esthesis.avro.EsthesisDataMessage;
import esthesis.avro.MessageTypeEnum;
import esthesis.avro.ValueData;
import esthesis.core.common.AppConstants.Device.Status;
import esthesis.core.common.data.DataUtils.ValueType;
import esthesis.core.common.exception.QDoesNotExistException;
import esthesis.dataflows.oriongateway.config.AppConfig;
import esthesis.dataflows.oriongateway.dto.OrionAttributeDTO;
import esthesis.dataflows.oriongateway.dto.OrionEntityDTO;
import esthesis.dataflows.oriongateway.service.OrionClientService.ATTRIBUTE_TYPE;
import esthesis.service.device.entity.DeviceAttributeEntity;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.device.resource.DeviceSystemResource;
import esthesis.util.redis.RedisUtils;
import esthesis.util.redis.RedisUtils.KeyType;
import io.quarkus.cache.CacheResult;
import io.quarkus.qute.Qute;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Transactional
@ApplicationScoped
public class OrionGatewayService {

	@Inject
	AppConfig appConfig;

	@Inject
	@RestClient
	DeviceSystemResource deviceSystemResource;

	@Inject
	OrionClientService orionClientService;

	@Inject
	RedisUtils redisUtils;

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
	 * //TODO check comment
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
		// Check if the service is configured to perform data updates.
		if (!appConfig.orionUpdateData()) {
			log.trace("Data updates are disabled for this service.");
			return false;
		}

		// check if device doesn't have a custom attribute for format and saving entities in Orion
		// if it has then it should skip registration check
		if (appConfig.orionCustomEntityJsonFormatAttributeName().isEmpty()) {
			// Check whether this device is registered in Orion.
			String orionId = generateOrionDeviceId(esthesisHardwareId);
			OrionEntityDTO orionEntity = orionClientService.getEntityByOrionId(orionId);
			if (orionEntity == null) {
				log.trace("Device with esthesis hardware ID '{}' is not registered in Orion.",
					esthesisHardwareId);
				return false;
			}
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
	 * @param esthesisHardwareId The ID to use if no configuration options specify otherwise.
	 * @param deviceAttributes   The esthesis device attributes.
	 */
	private String generateOrionDeviceId(String esthesisHardwareId,
																			 List<DeviceAttributeEntity> deviceAttributes) {
		if (appConfig.orionIdAttribute().isEmpty()) {
			return appConfig.orionIdPrefix() + esthesisHardwareId;
		} else {
			String idAttribute = appConfig.orionIdAttribute().get();
			return deviceAttributes.stream()
				.filter(attribute -> attribute.getAttributeName().equals(idAttribute))
				.map(DeviceAttributeEntity::getAttributeValue).findFirst()
				.orElse(appConfig.orionIdPrefix() + esthesisHardwareId);
		}
	}

	/**
	 * Generates an Orion device ID using the configuration attributes.
	 *
	 * @param esthesisHardwareId The ID to use if no configuration options specify otherwise.
	 * @return The Orion device ID.
	 */
	private String generateOrionDeviceId(String esthesisHardwareId) {
		if (appConfig.orionIdAttribute().isEmpty()) {
			return appConfig.orionIdPrefix() + esthesisHardwareId;

		} else {
			String nameAttribute = appConfig.orionIdAttribute().get();
			return deviceSystemResource
				.getDeviceAttributeByEsthesisHardwareIdAndAttributeName(esthesisHardwareId, nameAttribute)
				.map(DeviceAttributeEntity::getAttributeValue)
				.orElse(appConfig.orionIdPrefix() + esthesisHardwareId);
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
		List<DeviceAttributeEntity> esthesisDeviceAttributes =
			deviceSystemResource.getDeviceAttributesByEsthesisId(esthesisId);

		// Check whether this device should be registered.
		if (!isRegistrationAllowed(esthesisDeviceAttributes)) {
			log.debug("The attributes specified for device with esthesis ID '{}' do not allow "
				+ "registration.", esthesisId);
			return;
		}

		// Find the esthesis device.
		DeviceEntity esthesisDevice = deviceSystemResource.findById(esthesisId);
		log.debug("Esthesis device to be registered: {}", esthesisDevice.getHardwareId());
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
		String orionId = generateOrionDeviceId(esthesisDevice.getHardwareId());
		if (orionClientService.getEntityByOrionId(orionId) != null) {
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
			DeviceAttributeEntity.builder().attributeName(appConfig.attributeEsthesisId())
				.attributeValue(esthesisDevice.getId().toHexString()).attributeType(ValueType.STRING)
				.build());
		// Add a custom attribute with the esthesis hardware ID.
		esthesisDeviceAttributes.add(
			DeviceAttributeEntity.builder().attributeName(appConfig.attributeEsthesisHardwareId())
				.attributeValue(esthesisDevice.getHardwareId()).attributeType(ValueType.STRING)
				.build());

		// Prevent unwanted attributes from being sent to Orion
		filterAttributes(esthesisDeviceAttributes);

		orionEntity.setAttributes(OrionEntityDTO.attributesFromEsthesisDeviceAttributes(esthesisDeviceAttributes));

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

		// Prevent unwanted attributes from being sent to Orion
		filterAttributes(esthesisAttributes);

		// Get device entity
		DeviceEntity esthesisDevice = deviceSystemResource.findById(esthesisId);

		// Get Orion attributes.
		String orionId = generateOrionDeviceId(esthesisDevice.getHardwareId());
		OrionEntityDTO orionEntity = orionClientService.getEntityByOrionId(orionId);

		if (orionEntity == null) {
			log.warn("Device with esthesis ID '{}' not found in Orion, skipping sync.", esthesisId);
			return;
		}

		// Find all Orion attributes managed by esthesis.
		List<DeviceAttributeEntity> esthesisManagedAttributes = esthesisAttributes.stream().filter(
			deviceAttribute -> !deviceAttribute.getAttributeName()
				.equals(appConfig.attributeEsthesisId())).toList();

		// Add all esthesis attributes in Orion.
		for (DeviceAttributeEntity esthesisAttribute : esthesisManagedAttributes) {
			log.debug("Setting attribute '{}' to value '{}' in Orion.",
				esthesisAttribute.getAttributeName(), esthesisAttribute.getAttributeValue());
			orionClientService.setAttribute(orionEntity.getId(), esthesisAttribute.getAttributeName(),
				esthesisAttribute.getAttributeValue(), esthesisAttribute.getAttributeType(),
				ATTRIBUTE_TYPE.ATTRIBUTE);
		}

		// For every esthesis managed attribute in Orion, if it does not exist in esthesis, delete it.
		for (OrionAttributeDTO orionAttribute : orionEntity.getAttributes()) {
			if (!orionAttribute.getName().equals(appConfig.attributeEsthesisId())
				&& esthesisManagedAttributes.stream()
				.noneMatch(attribute -> attribute.getAttributeName().equals(orionAttribute.getName()))) {
				log.debug("Deleting attribute '{}' from Orion.", orionAttribute.getName());
				// Delete the attribute.
				orionClientService.deleteAttribute(orionEntity.getId(), orionAttribute.getName());
			}
		}
	}

	// Filters out configuration attributes from the list of device attributes.
	// If orionAttributesToSync is specified, retains only the attributes listed in orionAttributesToSync.
	private void filterAttributes(List<DeviceAttributeEntity> esthesisAttributes) {
		// Create a list of attributes to be filtered if any are defined
		List<String> filterAttributes = getConfiguredFilterAttributes();

		if (filterAttributes.isEmpty()) {
			// Create a list with all configuration attributes
			List<String> configurationAttributes = Stream.of(
					appConfig.orionCustomEntityJsonFormatAttributeName(),
					appConfig.orionUpdateDataAttribute(),
					appConfig.orionRegistrationEnabledAttribute(),
					appConfig.orionAttributesToSync()
				)
				.flatMap(Optional::stream)
				.toList();

			// Remove attributes that match any configuration attribute
			esthesisAttributes.removeIf(deviceAttributeEntity ->
				configurationAttributes.stream()
					.anyMatch(configAttr ->
						deviceAttributeEntity.getAttributeName().equalsIgnoreCase(configAttr)));
		} else {
			// Remove attributes that do not match any of the filter attributes
			esthesisAttributes.removeIf(deviceAttributeEntity ->
				filterAttributes.stream()
					.noneMatch(filterValue ->
						deviceAttributeEntity
							.getAttributeName()
							.toLowerCase()
							.contains(filterValue.toLowerCase())));
		}
	}

	private List<String> getConfiguredFilterAttributes() {
		return appConfig.orionAttributesToSync()
			.stream()
			.flatMap(s -> Stream.of(s.split(",")))
			.map(String::trim)
			.toList();
	}

	public void deleteEntityByEsthesisId(String esthesisId) {
		// Get device entity
		DeviceEntity esthesisDevice = deviceSystemResource.findById(esthesisId);

		// Find the device in Orion.
		String orionId = generateOrionDeviceId(esthesisDevice.getHardwareId());
		OrionEntityDTO orionEntity = orionClientService.getEntityByOrionId(orionId);
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
			ATTRIBUTE_TYPE attributeType;
			if (esthesisMessage.getType() == MessageTypeEnum.T) {
				attributeType = ATTRIBUTE_TYPE.TELEMETRY;
			} else if (esthesisMessage.getType() == MessageTypeEnum.M) {
				attributeType = ATTRIBUTE_TYPE.METADATA;
			} else {
				log.warn("Message type '{}' not supported, skipping.", esthesisMessage.getType());
				return;
			}

			// Extract category and timestamp from the observed value
			String category = esthesisMessage.getPayload().getCategory();
			String timestamp = esthesisMessage.getPayload().getTimestamp();

			// Check if device has set the custom measurement formatter attribute
			Optional<DeviceAttributeEntity> customOrionEntityJsonAttribute =
				appConfig.orionCustomEntityJsonFormatAttributeName()
					.flatMap(name -> deviceSystemResource.getDeviceAttributeByEsthesisHardwareIdAndAttributeName(esthesisHardwareId, name));

			// Retrieve the list attribute to be filtered, if any
			List<String> filterAttributes = getConfiguredFilterAttributes();

			esthesisMessage.getPayload().getValues().forEach((valueData) -> {

				String redisKey = getRedisKey(valueData, esthesisHardwareId, category);

				if (canSendAttribute(valueData, filterAttributes, redisKey)) {
					// if custom formatter is set, generate the dynamic json using the Qute formatter
					// along with the relevant measurement data
					if (customOrionEntityJsonAttribute.isPresent()) {
						String customFormattedValue =
							getCustomFormattedValue(valueData, customOrionEntityJsonAttribute, category, timestamp, esthesisHardwareId);
						orionClientService.saveOrUpdateEntities(customFormattedValue);
					} else {
						String orionId = generateOrionDeviceId(esthesisHardwareId);
						orionClientService.setAttribute(orionId, category + "." + valueData.getName(),
							valueData.getValue(), ValueType.valueOf(valueData.getValueType().name()), attributeType);
					}

					if (hasForwardingIntervalSet()) {
						saveOnRedis(redisKey);
					}

				}
			});
		} else {
			log.debug("esthesisHardwareId {} is not allowed to update data on orion", esthesisHardwareId);
		}
	}

	private void saveOnRedis(String redisKey) {
		String expirationString = Instant.now().plus(getOrionForwardingIntervalValue(), ChronoUnit.SECONDS).toString();
		redisUtils.setToHash(KeyType.ESTHESIS_DFLRI, redisKey, "expiration", expirationString);
		redisUtils.setExpirationForHash(KeyType.ESTHESIS_DFLRI, redisKey, getOrionForwardingIntervalValue());
		log.debug("Storing key {} in redis with expiration to {}", redisKey, expirationString);
	}

	private static String getCustomFormattedValue(ValueData valueData, Optional<DeviceAttributeEntity> customOrionEntityJsonAttribute, String category, String timestamp, String esthesisHardwareId) {
		return Qute.fmt(customOrionEntityJsonAttribute.get().getAttributeValue(),
				Map.of("category", category,
					"timestamp", timestamp,
					"hardwareId", esthesisHardwareId,
					"measurementName", valueData.getName(),
					"measurementValue", valueData.getValue())
			);
	}

	private String getRedisKey(ValueData valueData, String esthesisHardwareId, String category) {
		String redisKey = "orion." + esthesisHardwareId + "." + category + "." + valueData.getName();
		return redisKey.toLowerCase().trim();
	}

	// Check if attribute shall be sent to orion
	private boolean canSendAttribute(ValueData valueData, List<String> filterAttributes, String redisKey) {

		// check if  attribute is expected to be sent
		if (filterAttributes.isEmpty() || filterAttributes.stream().anyMatch(filter ->
			StringUtils.equals(valueData.getName(), filter.trim()))) {

			// check if it is the right time to send if custom interval is set
			if (hasForwardingIntervalSet()) {
				var hash = redisUtils.getHash(KeyType.ESTHESIS_DFLRI, redisKey);
				return hash.isEmpty();
			}

			return true;

		}
		return false;
	}

	private int getOrionForwardingIntervalValue(){
		String interval = appConfig.orionForwardingInterval().orElse("0");
		return Integer.parseInt(interval);
	}

	// check if the forwarding interval is set
	private boolean hasForwardingIntervalSet() {
		return  getOrionForwardingIntervalValue() > 0 && appConfig.redisUrl().isPresent();
	}

}
