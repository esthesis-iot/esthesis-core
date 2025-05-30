package esthesis.services.device.impl.service;

import static esthesis.core.common.AppConstants.HARDWARE_ID_REGEX;
import static esthesis.core.common.AppConstants.Security.Category.DEVICE;
import static esthesis.core.common.AppConstants.Security.Operation.CREATE;
import static esthesis.core.common.AppConstants.Security.Operation.WRITE;

import esthesis.common.crypto.dto.CreateCertificateRequestDTO;
import esthesis.common.data.DataUtils;
import esthesis.common.data.DataUtils.ValueType;
import esthesis.common.exception.QAlreadyExistsException;
import esthesis.common.exception.QDisabledException;
import esthesis.common.exception.QDoesNotExistException;
import esthesis.common.exception.QMismatchException;
import esthesis.common.exception.QSecurityException;
import esthesis.common.util.EsthesisCommonConstants;
import esthesis.core.common.AppConstants;
import esthesis.core.common.AppConstants.Device.Status;
import esthesis.core.common.AppConstants.DeviceRegistrationMode;
import esthesis.core.common.AppConstants.NamedSetting;
import esthesis.core.common.entity.BaseEntity;
import esthesis.service.crypto.resource.KeyResource;
import esthesis.service.device.dto.DeviceKeyDTO;
import esthesis.service.device.dto.DeviceRegistrationDTO;
import esthesis.service.device.entity.DeviceAttributeEntity;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.security.annotation.ErnPermission;
import esthesis.service.settings.entity.SettingEntity;
import esthesis.service.settings.resource.SettingsResource;
import esthesis.service.tag.entity.TagEntity;
import esthesis.service.tag.resource.TagResource;
import esthesis.services.device.impl.repository.DeviceAttributeRepository;
import esthesis.services.device.impl.repository.DeviceRepository;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Action;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Component;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Subject;
import esthesis.util.kafka.notifications.outgoing.KafkaNotification;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.operator.OperatorCreationException;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Service for registering devices.
 */
@Slf4j
@Transactional
@ApplicationScoped
public class DeviceRegistrationService {

	@Inject
	DeviceRepository deviceRepository;

	@Inject
	DeviceAttributeRepository deviceAttributeRepository;

	@Inject
	@RestClient
	KeyResource keyResource;

	@Inject
	@RestClient
	TagResource tagResource;

	@Inject
	@RestClient
	SettingsResource settingsResource;

	/**
	 * Checks if device-pushed tags exist in the system and report the ones that do not exist,
	 * optionally creating missing tag.s
	 *
	 * @param hardwareId    The hardware id of the device sending the tags.
	 * @param tags          The list of tag names to check.
	 * @param createMissing Whether to create missing tags or not.
	 * @return Returns the list of tags that can be assigned to the device, comprising of either
	 * existing tags or newly created tags.
	 */
	private List<String> checkTags(String hardwareId, List<String> tags, boolean createMissing) {
		List<String> validTags = new ArrayList<>();

		for (String tag : tags) {
			if (tagResource.findByName(tag) == null) {
				log.warn("Device-pushed tag '{}' for device with hardware id '{}' does not exist.", tag,
					hardwareId);
				if (createMissing) {
					log.debug("Creating missing tag '{}' for device with hardware id '{}'.", tag, hardwareId);
					// Check if tag name conforms to the being composed of numbers, letters, and underscore
					// only.
					String regex = "^[a-zA-Z0-9_-]+$";
					if (!tag.matches(regex)) {
						log.warn("Tag name '{}' does not conform to the naming convention '{}'. Tag will be "
							+ "ignored.", tag, regex);
					} else {
						TagEntity tagEntity = new TagEntity();
						tagEntity.setName(tag);
						tagResource.save(tagEntity);
						validTags.add(tag);
					}
				}
			} else {
				validTags.add(tag);
			}
		}

		return validTags;
	}

	/**
	 * Check if a registration secret is needed.
	 *
	 * @param registrationSecret The registration secret to check.
	 */
	private void checkRegistrationSecret(String registrationSecret) {
		if (settingsResource.findByName(NamedSetting.DEVICE_REGISTRATION_MODE).asString()
			.equals(DeviceRegistrationMode.OPEN_WITH_SECRET.toString())) {
			String platformRegistrationSecret = settingsResource.findByName(
				NamedSetting.DEVICE_REGISTRATION_SECRET).asString();
			if (!platformRegistrationSecret.equals(registrationSecret)) {
				throw new QSecurityException("The provided registration secret '{}' is incorrect.",
					registrationSecret);
			}
		}
	}

	/**
	 * Check if the given hardware id is valid.
	 *
	 * @param hardwareId The hardware id to check.
	 */
	private void checkHardwareId(String hardwareId) {
		if (!hardwareId.matches(HARDWARE_ID_REGEX)) {
			throw new QMismatchException(
				"Hardware id '{}' does not conform to the naming convention '{}'.", hardwareId,
				HARDWARE_ID_REGEX);
		}
	}

	/**
	 * Check if a device with the same hardware ID does not already exist.
	 *
	 * @param hardwareId The hardware id to check.
	 */
	private void checkIfDeviceExists(String hardwareId) {
		// Check that a device with the same hardware ID does not already exist.
		if (deviceRepository.findByHardwareIds(hardwareId).isPresent()) {
			throw new QAlreadyExistsException(
				"A device with hardware id '{}' is already registered with the platform.", hardwareId);
		}
	}

	/**
	 * The internal registration handler.
	 *
	 * @param hardwareId The hardware id of the device to be registered.
	 * @param tags       The tag names associated with this device as a comma-separated list.
	 */
	@ErnPermission(category = DEVICE, operation = CREATE)
	@KafkaNotification(component = Component.DEVICE, subject = Subject.DEVICE, action = Action.CREATE,
		idParamRegEx = "BaseEntity\\(id=(.*?)\\)")
	DeviceEntity register(String hardwareId, List<String> tags, AppConstants.Device.Status status,
		EsthesisCommonConstants.Device.Type deviceType, String registrationSecret, String attributes)
	throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, OperatorCreationException,
				 NoSuchProviderException {
		log.trace("Registering device with hardware id '{}', tags '{}', status '{}', "
				+ "secret '{}', and attributes '{}'.", hardwareId, tags, status, registrationSecret,
			attributes);

		// Check if a registration secret is needed.
		checkRegistrationSecret(registrationSecret);

		// Check the proposed hardware ID conforms to the naming convention.
		checkHardwareId(hardwareId);

		// Check that a device with the same hardware ID does not already exist.
		checkIfDeviceExists(hardwareId);

		// Create a keypair for the device to be registered.
		KeyPair keyPair = keyResource.generateKeyPair();

		// Set the security keys for the new device.
		final DeviceKeyDTO deviceKeyDTO = new DeviceKeyDTO().setPublicKey(
				keyResource.publicKeyToPEM(keyPair.getPublic()))
			.setPrivateKey(keyResource.privateKeyToPEM(keyPair.getPrivate())).setRolledOn(Instant.now())
			.setRolledOn(Instant.now()).setRolledAccepted(true);

		// Create a certificate for this device if the root CA is set.
		SettingEntity deviceRootCA = settingsResource.findByName(NamedSetting.DEVICE_ROOT_CA);
		if (deviceRootCA != null) {
			// Generate a certificate for this device.
			deviceKeyDTO.setCertificate(keyResource.generateCertificateAsPEM(
				new CreateCertificateRequestDTO().setCn(hardwareId).setKeyPair(keyPair)
					.setIncludeCertificateChain(true)));
			// Add a reference to the root CA.
			deviceKeyDTO.setCertificateCaId(
				settingsResource.findByName(NamedSetting.DEVICE_ROOT_CA).asString());
		} else {
			log.warn("No root CA is set to create a device certificates for device with hardware id "
				+ "'{}'.", hardwareId);
		}

		// Create the new device.
		ObjectId newDeviceId = new ObjectId();
		final DeviceEntity deviceEntity = new DeviceEntity()
			.setHardwareId(hardwareId)
			.setStatus(status)
			.setType(deviceType)
			.setCreatedOn(Instant.now())
			.setDeviceKey(deviceKeyDTO);
		deviceEntity.setId(newDeviceId);
		if (status != Status.PREREGISTERED) {
			deviceEntity.setRegisteredOn(Instant.now());
		}

		// Set device-pushed tags by converting the tag names to tag ids.
		if (!CollectionUtils.isEmpty(tags)) {
			List<String> validTags = checkTags(hardwareId, tags,
				settingsResource.findByName(NamedSetting.DEVICE_PUSHED_TAGS).asBoolean());
			deviceEntity.setTags(validTags.stream()
				.map(tagResource::findByName)
				.filter(Objects::nonNull)
				.map(TagEntity::getId)
				.map(Object::toString)
				.toList());
		}

		// Create device attributes.
		if (StringUtils.isNotBlank(attributes)) {
			String[] attributePair = attributes.split(",");
			for (String attribute : attributePair) {
				String[] attributeParts = attribute.split("=");
				if (attributeParts.length != 2) {
					throw new QMismatchException("Invalid attribute '{}'.", attribute);
				}
				// Register the attributes using the repository (instead of the service) to avoid issuing
				// notifications for new attributes registration before the new device is registered into
				// the platform.
				String attributeName = attributeParts[0];
				String[] attributeValuePair = attributeParts[1].split(";");
				String attributeValue = attributeValuePair[0];
				ValueType attributeType =
					attributeValuePair.length > 1 ? ValueType.valueOf(attributeValuePair[1].toUpperCase())
						: DataUtils.detectValueType(attributeValuePair[0]);

				deviceAttributeRepository.persist(
					DeviceAttributeEntity.builder().deviceId(newDeviceId.toHexString())
						.attributeName(attributeName).attributeValue(attributeValue)
						.attributeType(attributeType).build());
			}
		}

		// Create device.
		deviceRepository.persist(deviceEntity);

		return deviceEntity;
	}

	/**
	 * Preregisters a device, so that it can self-register later on. DeviceRegistrationDTO.hardwareId
	 * may content multiple devices in this case, separated by new lines. *
	 *
	 * @param deviceRegistration The preregistration details of the device.
	 */
	@ErnPermission(category = DEVICE, operation = CREATE)
	public List<DeviceEntity> preregister(DeviceRegistrationDTO deviceRegistration)
	throws NoSuchAlgorithmException, OperatorCreationException, InvalidKeySpecException,
				 NoSuchProviderException, IOException {
		// Split IDs.
		String ids = deviceRegistration.getHardwareId();
		ids = ids.replace("\n", ",");
		String[] idList = ids.split(",");

		// Before preregistering the devices check that all given registration IDs
		// are available. If any of the given IDs is already assigned on an
		// existing device abort the preregistration.
		for (String hardwareId : idList) {
			if (deviceRepository.findByHardwareIds(hardwareId).isPresent()) {
				throw new QAlreadyExistsException("Preregistration id '{}' is already assigned to a device "
					+ "registered in the system.", hardwareId);
			}
		}

		// Register IDs.
		List<DeviceEntity> preregisteredDevices = new ArrayList<>();
		for (String hardwareId : idList) {
			log.trace("Requested to preregister a device with hardware id '{}'.", hardwareId);
			preregisteredDevices.add(
				register(hardwareId, deviceRegistration.getTags(), Status.PREREGISTERED,
					deviceRegistration.getType(), deviceRegistration.getRegistrationSecret(), null));
		}

		return preregisteredDevices;
	}

	/**
	 * The public entrypoint to register a device.
	 *
	 * @param deviceRegistration The registration details of the device.
	 * @return The registered device.
	 * @throws IOException               If an error occurs during registration.
	 * @throws InvalidKeySpecException   If an error occurs during registration.
	 * @throws NoSuchAlgorithmException  If an error occurs during registration.
	 * @throws OperatorCreationException If an error occurs during registration.
	 * @throws NoSuchProviderException   If an error occurs during registration.
	 */
	@ErnPermission(category = DEVICE, operation = CREATE)
	public DeviceEntity register(DeviceRegistrationDTO deviceRegistration)
	throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, OperatorCreationException,
				 NoSuchProviderException {
		log.debug("Attempting to register device with '{}'.", deviceRegistration);

		DeviceRegistrationMode deviceRegistrationMode = DeviceRegistrationMode.valueOf(
			settingsResource.findByName(NamedSetting.DEVICE_REGISTRATION_MODE).asString());

		if (deviceRegistrationMode == DeviceRegistrationMode.DISABLED) {
			throw new QDisabledException("Registration of new devices is disabled.",
				deviceRegistration.getHardwareId());
		} else {
			// Check registration preconditions and register device.
			log.debug("Platform running in '{}' registration mode.", deviceRegistrationMode);
			return switch (deviceRegistrationMode) {
				case OPEN, OPEN_WITH_SECRET ->
					register(deviceRegistration.getHardwareId(), deviceRegistration.getTags(),
						Status.REGISTERED, deviceRegistration.getType(),
						deviceRegistration.getRegistrationSecret(), deviceRegistration.getAttributes());
				case ID -> activatePreregisteredDevice(deviceRegistration.getHardwareId());
				default ->
					throw new QDoesNotExistException("The requested registration mode does not exist.");
			};
		}
	}

	/**
	 * Activate a preregistered device. There is no actual device registration taking place here as
	 * the device already exists in system's database.
	 *
	 * @param hardwareId The hardware id of the device to activate.
	 * @return The activated device.
	 */
	@ErnPermission(category = DEVICE, operation = WRITE)
	public DeviceEntity activatePreregisteredDevice(String hardwareId) {
		Optional<DeviceEntity> optionalDevice = deviceRepository.findByHardwareIds(hardwareId);

		// Check that a device with the same hardware ID is not already registered.
		if (optionalDevice.isPresent() && !optionalDevice.get().getStatus()
			.equals(AppConstants.Device.Status.PREREGISTERED)) {
			throw new QSecurityException(
				"Cannot register device with hardwareId {} as it is already in {} state.", hardwareId,
				optionalDevice.get().getStatus());
		} else if (!optionalDevice.isPresent()) {
			throw new QSecurityException("Device with hardware ID {} does not exist.", hardwareId);
		}

		// Find the device and set its status to registered.
		DeviceEntity deviceEntity = optionalDevice.get();
		deviceEntity.setStatus(Status.REGISTERED);
		deviceEntity.setRegisteredOn(Instant.now());
		deviceRepository.update(deviceEntity);

		return deviceEntity;
	}
}
