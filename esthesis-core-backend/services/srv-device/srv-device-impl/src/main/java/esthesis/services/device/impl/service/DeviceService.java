package esthesis.services.device.impl.service;

import static esthesis.core.common.AppConstants.Security.Category.DEVICE;
import static esthesis.core.common.AppConstants.Security.Operation.CREATE;
import static esthesis.core.common.AppConstants.Security.Operation.DELETE;
import static esthesis.core.common.AppConstants.Security.Operation.READ;
import static esthesis.core.common.AppConstants.Security.Operation.WRITE;

import esthesis.common.avro.AvroUtils;
import esthesis.common.avro.EsthesisDataMessage;
import esthesis.common.avro.MessageTypeEnum;
import esthesis.common.exception.QMismatchException;
import esthesis.core.common.AppConstants.Device.Status;
import esthesis.core.common.AppConstants.NamedSetting;
import esthesis.service.common.BaseService;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.device.dto.DeviceProfileDTO;
import esthesis.service.device.dto.DeviceProfileFieldDataDTO;
import esthesis.service.device.dto.DevicesLastSeenStatsDTO;
import esthesis.service.device.dto.GeolocationDTO;
import esthesis.service.device.dto.ImportDataProcessingInstructionsDTO;
import esthesis.service.device.entity.DeviceAttributeEntity;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.security.annotation.ErnPermission;
import esthesis.service.settings.entity.DevicePageFieldEntity;
import esthesis.service.settings.entity.SettingEntity;
import esthesis.service.settings.resource.SettingsResource;
import esthesis.services.device.impl.repository.DeviceAttributeRepository;
import esthesis.services.device.impl.repository.DeviceRepository;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Action;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Component;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Subject;
import esthesis.util.kafka.notifications.outgoing.KafkaNotification;
import esthesis.util.redis.RedisUtils;
import esthesis.util.redis.RedisUtils.KeyType;
import io.opentelemetry.context.Context;
import io.quarkus.qute.Qute;
import io.smallrye.reactive.messaging.TracingMetadata;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.awaitility.Awaitility;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@Transactional
@ApplicationScoped
public class DeviceService extends BaseService<DeviceEntity> {

	@Inject
	DeviceRepository deviceRepository;

	@Inject
	DeviceAttributeRepository deviceAttributeRepository;

	@Inject
	@RestClient
	SettingsResource settingsResource;

	@Inject
	RedisUtils redisUtils;

	@Inject
	AvroUtils avroUtils;

	@ConfigProperty(name = "quarkus.application.name")
	String appName;

	@Inject
	@Channel("esthesis-data-message")
	Emitter<EsthesisDataMessage> dataMessageEmitter;

	private List<DeviceProfileFieldDataDTO> getProfileFields(String deviceId) {
		List<DeviceProfileFieldDataDTO> fields = new ArrayList<>();

		// Get configured device profile fields data.
		List<DevicePageFieldEntity> devicePageFieldEntities = settingsResource.getDevicePageFields();

		// Find the value of each field.
		DeviceEntity deviceEntity = findById(deviceId);
		devicePageFieldEntities.stream().filter(DevicePageFieldEntity::isShown).forEach(field -> {
			DeviceProfileFieldDataDTO deviceProfileFieldDataDTO = new DeviceProfileFieldDataDTO();
			deviceProfileFieldDataDTO.setLabel(field.getLabel()).setIcon(field.getIcon()).setValueType(
				redisUtils.getFromHash(KeyType.ESTHESIS_DM, deviceEntity.getHardwareId(),
					field.getMeasurement())).setLastUpdate(
				redisUtils.getLastUpdate(KeyType.ESTHESIS_DM, deviceEntity.getHardwareId(),
					field.getMeasurement()));

			String value = redisUtils.getFromHash(KeyType.ESTHESIS_DM, deviceEntity.getHardwareId(),
				field.getMeasurement());
			if (StringUtils.isNotEmpty(field.getFormatter())) {
				deviceProfileFieldDataDTO.setValue(
					Qute.fmt(field.getFormatter()).data("val", value).render());
			} else {
				deviceProfileFieldDataDTO.setValue(value);
			}

			fields.add(deviceProfileFieldDataDTO);
		});

		return fields;
	}

	private DeviceEntity saveHandler(DeviceEntity entity) {
		return super.save(entity);
	}

	/**
	 * Finds a device by its hardware ID.
	 *
	 * @param hardwareId   The hardware ID to search by.
	 * @param partialMatch Whether the search for the hardware ID should be partial or not.
	 */
	@ErnPermission(category = DEVICE, operation = READ)
	public Optional<DeviceEntity> findByHardwareId(String hardwareId, boolean partialMatch) {
		if (partialMatch) {
			return deviceRepository.findByHardwareIdPartial(hardwareId);
		} else {
			return deviceRepository.findByHardwareId(hardwareId);
		}
	}

	/**
	 * Finds the devices by a list of hardware Ids.
	 *
	 * @param hardwareIds  The list of hardware IDs to check.
	 * @param partialMatch Whether the search for the hardware ID will be partial or not.
	 * @return Returns the list of devices matched.
	 */
	@ErnPermission(category = DEVICE, operation = READ)
	public List<DeviceEntity> findByHardwareId(List<String> hardwareIds, boolean partialMatch) {
		if (partialMatch) {
			return deviceRepository.findByHardwareIdPartial(hardwareIds);
		} else {
			return deviceRepository.findByHardwareId(hardwareIds);
		}
	}

	/**
	 * Counts the devices in a list of hardware Ids. Search takes place via an exact match algorithm.
	 *
	 * @param hardwareIds  The list of hardware Ids to check.
	 * @param partialMatch Whether the search for the hardware ID will be partial or not.
	 * @return The number of the devices in the list that matched.
	 */
	@ErnPermission(category = DEVICE, operation = READ)
	public long countByHardwareId(List<String> hardwareIds, boolean partialMatch) {
		if (partialMatch) {
			return deviceRepository.countByHardwareIdPartial(hardwareIds);
		} else {
			return deviceRepository.countByHardwareId(hardwareIds);
		}
	}

	/**
	 * Returns the last known geolocation attributes of a device.
	 *
	 * @param deviceId The device ID to search by.
	 */
	@ErnPermission(category = DEVICE, operation = READ)
	public GeolocationDTO getGeolocation(String deviceId) {
		SettingEntity settingEntityLon = settingsResource.findByName(NamedSetting.DEVICE_GEO_LON);
		SettingEntity settingEntityLat = settingsResource.findByName(NamedSetting.DEVICE_GEO_LAT);

		if (settingEntityLon != null && settingEntityLat != null) {
			String hardwareId = findById(deviceId).getHardwareId();
			String redisLat = redisUtils.getFromHash(KeyType.ESTHESIS_DM, hardwareId,
				settingEntityLat.getValue());
			String redisLon = redisUtils.getFromHash(KeyType.ESTHESIS_DM, hardwareId,
				settingEntityLon.getValue());
			Instant lastUpdateLat = redisUtils.getLastUpdate(KeyType.ESTHESIS_DM, hardwareId,
				settingEntityLat.getValue());
			Instant lastUpdateLon = redisUtils.getLastUpdate(KeyType.ESTHESIS_DM, hardwareId,
				settingEntityLon.getValue());
			if (redisLat != null && redisLon != null && lastUpdateLat != null && lastUpdateLon != null) {
				return new GeolocationDTO(new BigDecimal(redisLat), new BigDecimal(redisLon),
					lastUpdateLat.isAfter(lastUpdateLon) ? lastUpdateLat : lastUpdateLon);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	@ErnPermission(category = DEVICE, operation = READ)
	public String getPublicKey(String id) {
		return findById(id).getDeviceKey().getPublicKey();
	}

	@ErnPermission(category = DEVICE, operation = READ)
	public String getPrivateKey(String id) {
		return findById(id).getDeviceKey().getPrivateKey();
	}

	@ErnPermission(category = DEVICE, operation = READ)
	public String getCertificate(String id) {
		return findById(id).getDeviceKey().getCertificate();
	}

	@ErnPermission(category = DEVICE, operation = WRITE)
	@KafkaNotification(component = Component.DEVICE, subject = Subject.DEVICE_ATTRIBUTE,
		action = Action.UPDATE, idParamOrder = 0)
	public void saveProfile(String deviceId, DeviceProfileDTO deviceProfileDTO) {
		// Remove attributes no longer present.
		deviceAttributeRepository.deleteAttributesNotIn(deviceId,
			deviceProfileDTO.getAttributes().stream().map(DeviceAttributeEntity::getAttributeName)
				.toList());

		// Save the attributes.
		deviceProfileDTO.getAttributes().forEach(attribute -> {
			DeviceAttributeEntity deviceAttributeEntity = deviceAttributeRepository.findByDeviceIdAndName(
				deviceId, attribute.getAttributeName()).orElse(
				new DeviceAttributeEntity(new ObjectId(), deviceId));
			deviceAttributeEntity.setAttributeValue(attribute.getAttributeValue());
			deviceAttributeEntity.setAttributeName(attribute.getAttributeName());
			deviceAttributeEntity.setAttributeType(attribute.getAttributeType());
			deviceAttributeRepository.persistOrUpdate(deviceAttributeEntity);
		});
	}

	public DeviceProfileDTO getProfile(String deviceId) {
		DeviceProfileDTO deviceProfileDTO = new DeviceProfileDTO();
		deviceProfileDTO.setAttributes(deviceAttributeRepository.findByDeviceId(deviceId));
		deviceProfileDTO.setFields(getProfileFields(deviceId));

		return deviceProfileDTO;
	}

	public List<DeviceProfileFieldDataDTO> getDeviceData(String deviceId) {
		List<DeviceProfileFieldDataDTO> fields = new ArrayList<>();
		DeviceEntity deviceEntity = findById(deviceId);

		redisUtils.getHashTriplets(KeyType.ESTHESIS_DM, deviceEntity.getHardwareId())
			.forEach(triple -> {
				DeviceProfileFieldDataDTO deviceProfileFieldDataDTO = new DeviceProfileFieldDataDTO();
				deviceProfileFieldDataDTO.setLabel(triple.getLeft()).setValue(triple.getMiddle())
					.setLastUpdate(triple.getRight());
				fields.add(deviceProfileFieldDataDTO);
			});

		return fields;
	}

	public Optional<DeviceAttributeEntity> getDeviceAttributeByName(String deviceId,
		String attributeName) {
		return deviceAttributeRepository.findByDeviceIdAndName(deviceId, attributeName);
	}

	@Override
	@ErnPermission(category = DEVICE, operation = DELETE)
	@KafkaNotification(component = Component.DEVICE, subject = Subject.DEVICE,
		action = Action.DELETE, idParamOrder = 0)
	public boolean deleteById(String deviceId) {
		// Delete all the attributes for this device.
		deviceAttributeRepository.deleteByDeviceId(deviceId);

		// Delete the device.
		return super.deleteById(deviceId);
	}

	@ErnPermission(category = DEVICE, operation = READ)
	public List<String> getDevicesIds() {
		return getAll().stream().map(DeviceEntity::getId).map(ObjectId::toHexString).toList();
	}

	@ErnPermission(category = DEVICE, operation = WRITE)
	public void importData(String deviceId, BufferedReader reader, MessageTypeEnum messageType,
		ImportDataProcessingInstructionsDTO instructionsDTO)
	throws IOException {
		log.info("Importing '{}' data for device '{}'.", messageType, deviceId);

		// Collect data for the messages to be created.
		String hardwareId = findById(deviceId).getHardwareId();
		String kafkaTopic;
		if (messageType == MessageTypeEnum.T) {
			kafkaTopic = settingsResource.findByName(NamedSetting.KAFKA_TOPIC_TELEMETRY).getValue();
		} else if (messageType == MessageTypeEnum.M) {
			kafkaTopic = settingsResource.findByName(NamedSetting.KAFKA_TOPIC_METADATA).getValue();
		} else {
			throw new QMismatchException("Unknown message type '{}'.", messageType);
		}
		log.debug("Publishing imported data to Kafka topic '{}'.", kafkaTopic);

		// Split data into new lines and import each line.
		String line;
		int batchCounter = 0;
		int okCounter = 0;
		int errorCounter = 0;
		while ((line = reader.readLine()) != null) {
			log.debug("Processing line '{}'.", line);
			if (batchCounter >= instructionsDTO.getBatchSize() && instructionsDTO.getBatchDelay() > 0) {
				log.debug("Batch processing limit reached, will wait for {} msec.",
					instructionsDTO.getBatchDelay());
				Awaitility.await()
					.pollDelay(instructionsDTO.getBatchDelay(), TimeUnit.MILLISECONDS)
					.atLeast(instructionsDTO.getBatchDelay(), TimeUnit.MILLISECONDS)
					.until(() -> true);
				batchCounter = 0;
			}
			try {
				EsthesisDataMessage esthesisDataMessage = EsthesisDataMessage.newBuilder()
					.setId(UUID.randomUUID().toString())
					.setHardwareId(hardwareId)
					.setType(messageType)
					.setSeenAt(Instant.now().toString())
					.setSeenBy(appName)
					.setChannel("data-import")
					.setPayload(avroUtils.parsePayload(line))
					.build();
				log.debug("Parsed message to Avro message '{}'.", esthesisDataMessage);

				dataMessageEmitter.send(
					Message.of(esthesisDataMessage)
						.addMetadata(OutgoingKafkaRecordMetadata.<String>builder()
							.withTopic(kafkaTopic)
							.withKey(hardwareId)
							.build())
						.addMetadata(TracingMetadata.withCurrent(Context.current())));

				okCounter++;
			} catch (Exception e) {
				log.error("Failed to parse line.", e);
				errorCounter++;
			}
			batchCounter++;
		}

		log.info("Imported '{}' messages successfully, with '{}' errors.", okCounter, errorCounter);
	}

	@Override
	@ErnPermission(category = DEVICE, operation = READ)
	public Page<DeviceEntity> find(Pageable pageable, boolean partialMatch) {
		return super.find(pageable, partialMatch);
	}

	@Override
	@ErnPermission(category = DEVICE, operation = READ)
	public DeviceEntity findById(String id) {
		return super.findById(id);
	}

	@ErnPermission(category = DEVICE, operation = CREATE)
	public DeviceEntity saveNew(DeviceEntity entity) {
		return saveHandler(entity);
	}

	@ErnPermission(category = DEVICE, operation = WRITE)
	public DeviceEntity saveUpdate(DeviceEntity entity) {
		return saveHandler(entity);
	}

	public DevicesLastSeenStatsDTO getDeviceStats() {
		DevicesLastSeenStatsDTO devicesLastSeenStatsDTO = new DevicesLastSeenStatsDTO();
		devicesLastSeenStatsDTO.setTotal(getAll().size());
		devicesLastSeenStatsDTO.setDisabled(deviceRepository.countByStatus(Status.DISABLED));
		devicesLastSeenStatsDTO.setPreregistered(deviceRepository.countByStatus(Status.PREREGISTERED));
		devicesLastSeenStatsDTO.setRegistered(deviceRepository.countByStatus(Status.REGISTERED));

		devicesLastSeenStatsDTO.setSeenLastMonth(deviceRepository.countLastSeenAfter(
			Instant.now().minus(Duration.ofDays(30))));
		devicesLastSeenStatsDTO.setSeenLastWeek(deviceRepository.countLastSeenAfter(
			Instant.now().minus(Duration.ofDays(7))));
		devicesLastSeenStatsDTO.setSeenLastDay(deviceRepository.countLastSeenAfter(
			Instant.now().minus(Duration.ofDays(1))));
		devicesLastSeenStatsDTO.setSeenLastHour(deviceRepository.countLastSeenAfter(
			Instant.now().minus(Duration.ofHours(1))));
		devicesLastSeenStatsDTO.setSeenLastMinute(deviceRepository.countLastSeenAfter(
			Instant.now().minus(Duration.ofMinutes(1))));

		devicesLastSeenStatsDTO.setJoinedLastMonth(deviceRepository.countJoinedAfter(
			Instant.now().minus(Duration.ofDays(30))));
		devicesLastSeenStatsDTO.setJoinedLastWeek(deviceRepository.countJoinedAfter(
			Instant.now().minus(Duration.ofDays(7))));
		devicesLastSeenStatsDTO.setJoinedLastDay(deviceRepository.countJoinedAfter(
			Instant.now().minus(Duration.ofDays(1))));
		devicesLastSeenStatsDTO.setJoinedLastHour(deviceRepository.countJoinedAfter(
			Instant.now().minus(Duration.ofHours(1))));
		devicesLastSeenStatsDTO.setJoinedLastMinute(deviceRepository.countJoinedAfter(
			Instant.now().minus(Duration.ofMinutes(1))));

		return devicesLastSeenStatsDTO;
	}
}
