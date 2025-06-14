package esthesis.services.device.impl.resource;

import com.github.slugify.Slugify;
import esthesis.common.avro.MessageTypeEnum;
import esthesis.common.exception.QAlreadyExistsException;
import esthesis.common.exception.QDoesNotExistException;
import esthesis.common.exception.QExceptionWrapper;
import esthesis.core.common.AppConstants;
import esthesis.core.common.AppConstants.Device.DataImportType;
import esthesis.core.common.AppConstants.Device.Status;
import esthesis.core.common.AppConstants.Security.Category;
import esthesis.core.common.AppConstants.Security.Operation;
import esthesis.service.audit.ccc.Audited;
import esthesis.service.audit.ccc.Audited.AuditLogType;
import esthesis.service.common.paging.JSONReplyFilter;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.common.validation.CVEBuilder;
import esthesis.service.device.dto.DeviceDataImportDTO;
import esthesis.service.device.dto.DeviceProfileDTO;
import esthesis.service.device.dto.DeviceProfileFieldDataDTO;
import esthesis.service.device.dto.DeviceRegistrationDTO;
import esthesis.service.device.dto.DeviceTextDataImportDTO;
import esthesis.service.device.dto.GeolocationDTO;
import esthesis.service.device.dto.ImportDataProcessingInstructionsDTO;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.device.resource.DeviceResource;
import esthesis.services.device.impl.service.DeviceRegistrationService;
import esthesis.services.device.impl.service.DeviceService;
import esthesis.services.device.impl.service.DeviceTagService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.operator.OperatorCreationException;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link DeviceResource}.
 */
public class DeviceResourceImpl implements DeviceResource {

	private static final Logger log = LoggerFactory.getLogger(DeviceResourceImpl.class);
	@Inject
	DeviceService deviceService;

	@Inject
	DeviceTagService deviceTagService;

	@Inject
	DeviceRegistrationService deviceRegistrationService;

	/**
	 * Creates processing instructions for batch data imports.
	 *
	 * @param deviceDataImportDTO The DTO containing the batch processing instructions.
	 * @return The processing instructions.
	 */
	private ImportDataProcessingInstructionsDTO createBatchProcessingInstructions(
		DeviceDataImportDTO deviceDataImportDTO) {
		return ImportDataProcessingInstructionsDTO.builder()
			.batchSize(deviceDataImportDTO.getBatchSize())
			.batchDelay(deviceDataImportDTO.getBatchDelay())
			.build();
	}

	/**
	 * Import ELP data from a text value.
	 *
	 * @param deviceId        The device ID to import the data to.
	 * @param data            The data in ELP format to import. Data can be multiline. A line is
	 *                        considered to be terminated by any one of a line feed ('\n'), a carriage
	 *                        return ('\r'), a carriage return followed immediately by a line feed, or
	 *                        by reaching the end-of-file (EOF).
	 * @param messageType     The type of message to import.
	 * @param instructionsDTO The instructions for processing the data.
	 */
	private void importELPText(String deviceId, String data, MessageTypeEnum messageType,
		ImportDataProcessingInstructionsDTO instructionsDTO) {
		try (BufferedReader reader = new BufferedReader(new StringReader(data))) {
			deviceService.importData(deviceId, reader, messageType, instructionsDTO);
		} catch (IOException e) { //NOSONAR
			log.error("Error importing file.", e);
			throw new QExceptionWrapper("Could not import file.", e);
		}
	}

	/**
	 * Import ELP data from a file.
	 *
	 * @param deviceId        The device ID to import the data to.
	 * @param file            The file in ELP format to import. Data can be multiline. A line is
	 *                        considered to be terminated by any one of a line feed ('\n'), a carriage
	 *                        return ('\r'), a carriage return followed immediately by a line feed, or
	 *                        by reaching the end-of-file (EOF).
	 * @param messageType     The type of message to import.
	 * @param instructionsDTO The instructions for processing the data.
	 */
	private void importELPFile(String deviceId, FileUpload file, MessageTypeEnum messageType,
		ImportDataProcessingInstructionsDTO instructionsDTO) {
		try (BufferedReader reader = new BufferedReader(new FileReader(file.filePath().toFile()))) {
			deviceService.importData(deviceId, reader, messageType, instructionsDTO);
		} catch (IOException e) { //NOSONAR
			log.error("Error importing file.", e);
			throw new QExceptionWrapper("Could not import file", e);
		}
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.DEVICE, op = Operation.READ, msg = "Search devices", log =
		AuditLogType.DATA_IN)
	public Page<DeviceEntity> find(@BeanParam Pageable pageable) {
		return deviceService.find(pageable);
	}

	@GET
	@Override
	@Path("/v1/{id}")
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.DEVICE, op = Operation.READ, msg = "View device")
	public DeviceEntity get(@PathParam("id") String id) {
		return deviceService.findById(id);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.DEVICE, op = Operation.DELETE, msg = "Delete device")
	public void delete(@PathParam("id") String id) {
		deviceService.deleteById(id);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	public Long countByHardwareIds(
		@QueryParam("hardwareIds") String hardwareIds) {
		if (StringUtils.isBlank(hardwareIds)) {
			return 0L;
		} else {
			return deviceService.countByHardwareId(Arrays.asList(hardwareIds.split(",")));
		}
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	public List<DeviceEntity> findByHardwareIds(String hardwareIds) {
		return deviceService.findByHardwareIds(Arrays.asList(hardwareIds.split(",")));
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	public List<DeviceEntity> findByTagName(String tag) {
		return deviceTagService.findByTagName(tag);
	}

	@Override
	@RolesAllowed({AppConstants.ROLE_USER, AppConstants.ROLE_SYSTEM})
	public List<DeviceEntity> findByTagId(String tagId) {
		return deviceTagService.findByTagId(tagId);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	public Long countByTags(@QueryParam("tags") String tags) {
		return deviceTagService.countByTag(Arrays.asList(tags.split(",")));
	}

	@Override
	public Long countByStatus(Status status) {
		return deviceTagService.countByStatus(status);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	public GeolocationDTO getDeviceGeolocation(String deviceId) {
		return deviceService.getGeolocation(deviceId);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.DEVICE, op = Operation.READ, msg = "Download device", log =
		AuditLogType.DATA_IN)
	public Response download(String deviceId, AppConstants.KeyType type) {
		DeviceEntity deviceEntity = deviceService.findById(deviceId);

		String content;
		String filename = Slugify.builder().underscoreSeparator(true).build()
			.slugify(deviceEntity.getHardwareId());
		switch (type) {
			case PRIVATE -> {
				filename += ".key";
				content = deviceService.getPrivateKey(deviceId);
			}
			case PUBLIC -> {
				filename += ".pub";
				content = deviceService.getPublicKey(deviceId);
			}
			case CERTIFICATE -> {
				filename += ".crt";
				content = deviceService.getCertificate(deviceId);
			}
			default -> throw new QDoesNotExistException("Key type '{}' is not valid.", type);
		}

		return Response.ok(content)
			.header("Content-Disposition", "attachment; filename=" + filename)
			.build();
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	public DeviceProfileDTO getProfile(String deviceId) {
		return deviceService.getProfile(deviceId);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.DEVICE, op = Operation.WRITE, msg = "Save a device profile")
	public void saveProfile(String deviceId, DeviceProfileDTO deviceProfileDTO) {
		deviceService.saveProfile(deviceId, deviceProfileDTO);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	public List<DeviceProfileFieldDataDTO> getDeviceData(String deviceId) {
		return deviceService.getDeviceData(deviceId);
	}

	@Override
	@POST
	@Path("/v1/preregister")
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.DEVICE, op = Operation.WRITE, msg = "Preregistering device")
	@JSONReplyFilter(filter = "id,hardwareId,status")
	public List<DeviceEntity> preregister(@Valid DeviceRegistrationDTO deviceRegistration)
	throws NoSuchAlgorithmException, IOException, OperatorCreationException,
				 InvalidKeySpecException, NoSuchProviderException {
		try {
			return deviceRegistrationService.preregister(deviceRegistration);
		} catch (QAlreadyExistsException e) {
			throw CVEBuilder.addAndThrow("ids", "One or more IDs are already registered.");
		}
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.DEVICE, op = Operation.WRITE, msg = "Activating preregistered device")
	public DeviceEntity activatePreregisteredDevice(String hardwareId) {
		return deviceRegistrationService.activatePreregisteredDevice(hardwareId);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.DEVICE, op = Operation.WRITE, msg = "Save device")
	public Response saveTagsAndStatus(DeviceEntity deviceEntity) {
		deviceService.saveTagsAndStatus(deviceEntity);

		return Response.ok().build();
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.DEVICE, op = Operation.WRITE, msg = "Import device text data",
		log = AuditLogType.DATA_OUT)
	public void importDeviceDataFromText(String deviceId, DataImportType type,
		DeviceTextDataImportDTO deviceTextDataImportDTO) {
		switch (type) {
			case TELEMETRY -> importELPText(deviceId, deviceTextDataImportDTO.getData(),
				MessageTypeEnum.T, createBatchProcessingInstructions(deviceTextDataImportDTO));
			case METADATA -> importELPText(deviceId, deviceTextDataImportDTO.getData(),
				MessageTypeEnum.M, createBatchProcessingInstructions(deviceTextDataImportDTO));
			default -> throw new QDoesNotExistException("Data import type '{}' is not valid.", type);
		}
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.DEVICE, op = Operation.WRITE, msg = "Import device file data",
		log = AuditLogType.DATA_OUT)
	public void importDeviceDataFromFile(String deviceId, DataImportType type,
		DeviceDataImportDTO deviceDataImportDTO, FileUpload file) {
		switch (type) {
			case TELEMETRY -> importELPFile(deviceId, file, MessageTypeEnum.T,
				createBatchProcessingInstructions(deviceDataImportDTO));
			case METADATA -> importELPFile(deviceId, file, MessageTypeEnum.M,
				createBatchProcessingInstructions(deviceDataImportDTO));
			default -> throw new QDoesNotExistException("Data import type '{}' is not valid.", type);
		}
	}

}
