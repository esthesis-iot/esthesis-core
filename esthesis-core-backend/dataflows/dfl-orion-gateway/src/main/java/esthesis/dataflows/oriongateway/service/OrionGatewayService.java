package esthesis.dataflows.oriongateway.service;

import static esthesis.core.common.AppConstants.REDIS_KEY_SUFFIX_TIMESTAMP;

import esthesis.common.avro.EsthesisDataMessage;
import esthesis.common.avro.MessageTypeEnum;
import esthesis.common.avro.ValueData;
import esthesis.common.data.DataUtils.ValueType;
import esthesis.common.exception.QDoesNotExistException;
import esthesis.core.common.AppConstants.Device.Status;
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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Service for handling interactions with Orion.
 */
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
		List<DeviceAttributeEntity> esthesisDeviceAttributes
			= new ArrayList<>(deviceSystemResource.getDeviceAttributesByEsthesisId(esthesisId));

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
		log.debug("esthesis device to be registered: {}", esthesisDevice.getHardwareId());

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

		orionEntity.setAttributes(
			OrionEntityDTO.attributesFromEsthesisDeviceAttributes(esthesisDeviceAttributes));

		orionClientService.createEntity(orionEntity);
		log.info("Device '{}' of type '{}' successfully registered in Orion.", orionDeviceId,
			orionDeviceType);
	}

	/**
	 * Checks whether the device attributes allow registration.
	 *
	 * @param esthesisDeviceAttributes The device attributes.
	 * @return Whether registration is allowed.
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
	 * Generates an Orion device ID using the configuration attributes.
	 *
	 * @param esthesisHardwareId The ID to use if no configuration options specify otherwise.
	 * @return The Orion device ID.
	 */
	private String generateOrionDeviceId(String esthesisHardwareId) {
		if (appConfig.orionIdAttribute().isEmpty()) {
			return appConfig.orionIdPrefix() + esthesisHardwareId;
		} else {
			String nameAttribute = appConfig.orionIdAttribute().orElseThrow();
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
			String typeAttribute = appConfig.orionTypeAttribute().orElseThrow();
			return deviceAttributes.stream()
				.filter(attribute -> attribute.getAttributeName().equals(typeAttribute))
				.map(DeviceAttributeEntity::getAttributeValue).findFirst()
				.orElse(appConfig.orionDefaultType());
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
			String idAttribute = appConfig.orionIdAttribute().orElseThrow();
			return deviceAttributes.stream()
				.filter(attribute -> attribute.getAttributeName().equals(idAttribute))
				.map(DeviceAttributeEntity::getAttributeValue).findFirst()
				.orElse(appConfig.orionIdPrefix() + esthesisHardwareId);
		}
	}

	/**
	 * Filters out configuration attributes from the list of device attributes. If
	 * orionAttributesToSync is specified, retains only the attributes listed in
	 * orionAttributesToSync.
	 *
	 * @param esthesisAttributes The list of device attributes to filter.
	 */
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

	/**
	 * Returns a list of attributes to be filtered based on the configuration.
	 *
	 * @return The list of attributes to be filtered.
	 */
	private List<String> getConfiguredFilterAttributes() {
		return appConfig.orionAttributesToSync()
			.stream()
			.flatMap(s -> Stream.of(s.split(",")))
			.map(String::trim)
			.toList();
	}

	/**
	 * Synchronises esthesis device attributes with Orion. The source of synchronisation is esthesis
	 * and the target is Orion.
	 */
	public void syncAttributes(String esthesisId) {
		log.debug("Synchronising attributes for device with esthesis ID '{}'.", esthesisId);
		// Get esthesis device attributes.
		List<DeviceAttributeEntity> esthesisAttributes =
			new ArrayList<>(deviceSystemResource.getDeviceAttributesByEsthesisId(esthesisId));

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

		// For every esthesis-managed attribute in Orion, if it does not exist in esthesis, delete it.
		for (Map.Entry<String, OrionAttributeDTO> entry : orionEntity.getAttributes().entrySet()) {

			// The key is the attribute name.
			String attributeName = entry.getKey();

			if (!attributeName.equals(appConfig.attributeEsthesisId())
				&& esthesisManagedAttributes.stream()
				.noneMatch(attribute -> attribute.getAttributeName().equals(attributeName))) {

				log.debug("Deleting attribute '{}' from Orion.", attributeName);

				// Delete the attribute.
				orionClientService.deleteAttribute(orionEntity.getId(), attributeName);
			}
		}
	}

	/**
	 * Deletes an entity from Orion based on the esthesis ID.
	 *
	 * @param esthesisId The esthesis ID of the device to delete.
	 */
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

	/**
	 * Perform Orion updates.
	 *
	 * @param exchange The Camel exchange to extract the message from.
	 */
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
			processAttribute(esthesisMessage, esthesisHardwareId, attributeType);
		} else {
			log.debug("esthesisHardwareId {} is not allowed to update data on orion", esthesisHardwareId);
		}
	}

	/**
	 * Returns whether a/ the service is configured to perform data updates (i.e. update the metrics
	 * received by esthesis in Orion), b/ the specific device is already registered in Orion, and c/
	 * the specific device has an attribute allowing/disallowing data updates.
	 * <p>
	 * The result of this method remains cached for a short period of time (configured in application
	 * properties via the ESTHESIS_DFL_CACHE_IS_DATA_UPDATE_ALLOWED_EXPIRATION). This is to avoid
	 * unnecessary calls to Orion for every single piece of data received (which can be multiple per
	 * second).
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

		// Check if any custom Json format configuration exists.
		// If so, we must skip the check for the device in Orion.
		if (appConfig.orionCustomEntityJsonFormatAttributeName().isEmpty()
			&& appConfig.orionCustomEntityJsonFormat().isEmpty()) {
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
	 * Processes a device attribute.
	 *
	 * @param esthesisMessage    The esthesis message to process.
	 * @param esthesisHardwareId The esthesis hardware ID.
	 * @param attributeType      The attribute type.
	 */
	private void processAttribute(EsthesisDataMessage esthesisMessage,
		String esthesisHardwareId, ATTRIBUTE_TYPE attributeType) {
		// Extract category, timestamp and values from the observed value.
		String category = esthesisMessage.getPayload().getCategory();
		String timestamp = esthesisMessage.getPayload().getTimestamp();
		List<ValueData> values = esthesisMessage.getPayload().getValues();

		String customOrionEntityJsonFromDevice;
		String customOrionEntityJsonFromDataflow = null;

		// Check if device has set the custom measurement formatter attribute.
		customOrionEntityJsonFromDevice =
			appConfig.orionCustomEntityJsonFormatAttributeName()
				.flatMap(
					name -> deviceSystemResource.getDeviceAttributeByEsthesisHardwareIdAndAttributeName(
						esthesisHardwareId, name))
				.map(DeviceAttributeEntity::getAttributeValue)
				.orElse(null);

		// If device custom Json is not set, check if the dataflow has set the custom measurement formatter.
		if(StringUtils.isBlank(customOrionEntityJsonFromDevice)) {
			customOrionEntityJsonFromDevice = null;
			customOrionEntityJsonFromDataflow = appConfig.orionCustomEntityJsonFormat().orElse(null);
		}

		// Set null in case of empty string to avoid false positives.
		if(StringUtils.isBlank(customOrionEntityJsonFromDataflow)){
			customOrionEntityJsonFromDataflow = null;
		}

		// Get the custom Orion entity JSON value given precedence to the device attribute.
		final String customOrionEntityJson = customOrionEntityJsonFromDevice != null ?
			customOrionEntityJsonFromDevice : customOrionEntityJsonFromDataflow;

		// Retrieve the list attribute to be filtered, if any.
		List<String> filterAttributes = getConfiguredFilterAttributes();

		values.forEach(valueData ->
			validateAndSendAttribute(
				esthesisHardwareId,
				attributeType,
				valueData,
				category,
				filterAttributes,
				timestamp,
				customOrionEntityJson
			)
		);
	}

	/**
	 * Validates and sends an attribute to Orion.
	 *
	 * @param esthesisHardwareId             The esthesis hardware ID.
	 * @param attributeType                  The attribute type.
	 * @param valueData                      The value data.
	 * @param category                       The category.
	 * @param filterAttributes               The list of attributes to filter.
	 * @param timestamp                      The timestamp.
	 * @param customOrionEntityJson          The custom Orion entity JSON attribute.
	 */
	private void validateAndSendAttribute(String esthesisHardwareId,
		ATTRIBUTE_TYPE attributeType,
		ValueData valueData,
		String category,
		List<String> filterAttributes,
		String timestamp,
		String customOrionEntityJson) {
		String redisKey = getRedisKey(valueData, esthesisHardwareId, category);

		if (canSendAttribute(valueData, filterAttributes)) {
			if (hasForwardingIntervalSet()) {
				String latestTimestamp =
					redisUtils.getHash(KeyType.ESTHESIS_DFLRI, redisKey)
						.getOrDefault(REDIS_KEY_SUFFIX_TIMESTAMP, "");

				if (StringUtils.isNotBlank(latestTimestamp)) {
					// check if the latest timestamp is valid against the interval,
					// if so, send the attribute and update the latest timestamp on redis
					if (isTimestampValidAgainstInterval(latestTimestamp, timestamp)) {
						sendAttribute(esthesisHardwareId, attributeType, valueData,
							customOrionEntityJson, category, timestamp);
						saveTimestampOnRedis(redisKey, timestamp);

						// else check if the latest timestamp is valid past data.
						// if so, send the attribute but don't update the latest timestamp on redis
					} else if (isPastTimestampValidAgainstInterval(latestTimestamp, timestamp)) {
						sendAttribute(esthesisHardwareId, attributeType, valueData,
							customOrionEntityJson, category, timestamp);
					} else {
						log.debug(
							"Attribute {}  with timestamp {} was ignored as it is not valid against the forwarding interval rule",
							valueData.getName(), timestamp);
					}
				} else {
					sendAttribute(esthesisHardwareId, attributeType, valueData,
						customOrionEntityJson, category, timestamp);
					saveTimestampOnRedis(redisKey, timestamp);
				}
			} else {
				sendAttribute(esthesisHardwareId, attributeType, valueData, customOrionEntityJson,
					category, timestamp);
			}
		} else {
			log.debug("Attribute {} was ignored as it is not in the filter list", valueData.getName());
		}
	}

	/**
	 * Get the redis key for the requested device attribute.
	 *
	 * @param valueData          The value data.
	 * @param esthesisHardwareId The esthesis hardware ID.
	 * @param category           The category.
	 * @return The redis key.
	 */
	private String getRedisKey(ValueData valueData, String esthesisHardwareId, String category) {
		String redisKey = "orion." + esthesisHardwareId + "." + category + "." + valueData.getName();
		return redisKey.toLowerCase().trim();
	}

	/**
	 * Check if the attribute should be sent according to the configuration filter set by the user.
	 *
	 * @param valueData        The value data.
	 * @param filterAttributes The list of attributes to filter.
	 * @return Whether the attribute should be sent.
	 */
	private boolean canSendAttribute(ValueData valueData, List<String> filterAttributes) {
		return filterAttributes.isEmpty() || filterAttributes.stream().anyMatch(filter ->
			StringUtils.equals(valueData.getName(), filter.trim()));
	}

	/**
	 * Check if the forwarding interval is set and the redis url is set.
	 *
	 * @return Whether the forwarding interval is set.
	 */
	private boolean hasForwardingIntervalSet() {
		return getOrionForwardingIntervalValue() > 0 && appConfig.redisUrl().isPresent();
	}

	/**
	 * Check against the forwarding interval if the new timestamp is valid new data.
	 *
	 * @param latestTimestamp The latest timestamp.
	 * @param newTimestamp    The new timestamp.
	 * @return Whether the new timestamp is valid.
	 */
	private boolean isTimestampValidAgainstInterval(String latestTimestamp, String newTimestamp) {
		try {
			long timeDiffInSeconds = ChronoUnit.SECONDS.between(Instant.parse(latestTimestamp),
				Instant.parse(newTimestamp));
			return timeDiffInSeconds >= getOrionForwardingIntervalValue();
		} catch (Exception e) {
			log.error("Error while validating timestamp {} against latest timestamp {}", newTimestamp,
				latestTimestamp, e);
		}
		return false;
	}

	/**
	 * Send the attribute to Orion.
	 *
	 * @param esthesisHardwareId             The esthesis hardware ID.
	 * @param attributeType                  The attribute type.
	 * @param valueData                      The value data.
	 * @param customOrionEntityJson          The custom Orion entity JSON.
	 * @param category                       The category.
	 * @param timestamp                      The timestamp.
	 */
	private void sendAttribute(String esthesisHardwareId, ATTRIBUTE_TYPE attributeType,
		ValueData valueData, String customOrionEntityJson,
		String category, String timestamp) {
		// if custom formatter is set, generate the dynamic json using the Qute formatter
		// along with the relevant measurement data
		if (StringUtils.isNotBlank(customOrionEntityJson)) {
			String customFormattedValue =
				getCustomFormattedValue(valueData, customOrionEntityJson, category, timestamp,
					esthesisHardwareId);
			orionClientService.saveOrUpdateEntities(customFormattedValue);
		} else {
			String orionId = generateOrionDeviceId(esthesisHardwareId);
			orionClientService.setAttribute(orionId, category + "." + valueData.getName(),
				valueData.getValue(), ValueType.valueOf(valueData.getValueType().name()), attributeType);
		}
	}

	/**
	 * Save the timestamp on Redis.
	 *
	 * @param redisKey  The redis key.
	 * @param timestamp The timestamp.
	 */
	private void saveTimestampOnRedis(String redisKey, String timestamp) {
		redisUtils.setToHash(KeyType.ESTHESIS_DFLRI, redisKey, REDIS_KEY_SUFFIX_TIMESTAMP, timestamp);
		log.debug("Updating key {} in redis with value {}", redisKey, timestamp);
	}

	/**
	 * Check against the forwarding interval if the new timestamp is valid past data.
	 *
	 * @param latestTimestamp The latest timestamp.
	 * @param newTimestamp    The new timestamp.
	 * @return Whether the new timestamp is valid.
	 */
	private boolean isPastTimestampValidAgainstInterval(String latestTimestamp, String newTimestamp) {
		try {
			long timeDiffInSeconds = ChronoUnit.SECONDS.between(Instant.parse(latestTimestamp),
				Instant.parse(newTimestamp));
			return (timeDiffInSeconds + getOrionForwardingIntervalValue()) < 0;
		} catch (Exception e) {
			log.error("Error while validating timestamp {} against latest timestamp {}", newTimestamp,
				latestTimestamp, e);
		}
		return false;
	}

	/**
	 * Get the Orion forwarding interval value.
	 *
	 * @return The Orion forwarding interval value.
	 */
	private int getOrionForwardingIntervalValue() {
		String interval = appConfig.orionForwardingInterval().orElse("0");
		return Integer.parseInt(interval);
	}

	/**
	 * Get the custom formatted value.
	 *
	 * @param valueData                      The value data.
	 * @param customOrionEntityJson          The custom Orion entity JSON.
	 * @param category                       The category.
	 * @param timestamp                      The timestamp.
	 * @param esthesisHardwareId             The esthesis hardware ID.
	 * @return The custom formatted value.
	 */
	private static String getCustomFormattedValue(ValueData valueData,
		String customOrionEntityJson, String category,
		String timestamp, String esthesisHardwareId) {
		return Qute.fmt(customOrionEntityJson,
			Map.of("category", category,
				"timestamp", timestamp,
				"hardwareId", esthesisHardwareId,
				"measurementName", valueData.getName(),
				"measurementValue", valueData.getValue())
		);
	}

}
