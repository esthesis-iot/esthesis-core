package esthesis.services.device.impl.service;

import esthesis.common.util.EsthesisCommonConstants.Device.Type;
import esthesis.core.common.AppConstants.Device.Status;
import esthesis.core.common.entity.BaseEntity;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.tag.entity.TagEntity;
import esthesis.services.device.impl.repository.DeviceRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import org.instancio.Instancio;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import static esthesis.core.common.AppConstants.Device.Status.DISABLED;
import static esthesis.core.common.AppConstants.Device.Status.PREREGISTERED;
import static esthesis.core.common.AppConstants.Device.Status.REGISTERED;
import static org.instancio.Select.all;
import static org.instancio.Select.field;

@ApplicationScoped
public class TestHelper {

	@Inject
	DeviceRepository deviceRepository;

	public void clearDatabase() {
		deviceRepository.deleteAll();
	}


	public void createEntities() {
		DeviceEntity deviceCore1 =
			makeDeviceEntity("test-registered-device-core-1", REGISTERED, "tag1,tag2", Type.CORE);

		DeviceEntity deviceCore2 =
			makeDeviceEntity("test-registered-device-core-2", REGISTERED, "tag3,tag4", Type.CORE);

		DeviceEntity deviceCore3 =
			makeDeviceEntity("test-disabled-device-core-1", DISABLED, "tag5", Type.CORE);

		DeviceEntity deviceCore4 =
			makeDeviceEntity("test-preregistered-device-core-1",PREREGISTERED , "tag6", Type.CORE);

		DeviceEntity deviceEdge1 =
			makeDeviceEntity("test-registered-device-active-edge-1", REGISTERED, "tag1,tag2", Type.EDGE);

		DeviceEntity deviceEdge2 =
			makeDeviceEntity("test-registered-device-active-edge-2", REGISTERED, "tag3,tag4", Type.EDGE);

		DeviceEntity deviceEdge3 =
			makeDeviceEntity("test-disabled-device-edge-1", DISABLED, "tag5", Type.EDGE);

		DeviceEntity deviceEdge4 =
			makeDeviceEntity("test-preregistered-device-edge-1",PREREGISTERED , "tag6", Type.EDGE);

		deviceRepository.persist(deviceCore1);
		deviceRepository.persist(deviceCore2);
		deviceRepository.persist(deviceCore3);
		deviceRepository.persist(deviceCore4);
		deviceRepository.persist(deviceEdge1);
		deviceRepository.persist(deviceEdge2);
		deviceRepository.persist(deviceEdge3);
		deviceRepository.persist(deviceEdge4);
	}


	private DeviceEntity makeDeviceEntity(String hardwareId,
																				Status status,
																				String tags,
																				Type type) {
		return Instancio.of(DeviceEntity.class)
			.ignore(all(field(BaseEntity.class, "id")))
			.set(field(DeviceEntity.class, "hardwareId"), hardwareId)
			.set(field(DeviceEntity.class, "status"), status)
			.set(field(DeviceEntity.class, "tags"), Arrays.stream(tags.split(",")).toList())
			.set(field(DeviceEntity.class, "type"), type)
			.set(field(DeviceEntity.class, "createdOn"),  Instant.now().minus(1, ChronoUnit.DAYS))
			.set(field(DeviceEntity.class, "registeredOn"),  Instant.now().minus(12, ChronoUnit.HOURS))
			.set(field(DeviceEntity.class, "lastSeen"),  Instant.now().minus(5, ChronoUnit.MINUTES))
			.create();

	}

	public List<DeviceEntity> findAllDeviceEntity() {
		return deviceRepository.findAll().list();
	}

	public List<DeviceEntity> findAllRegisteredDeviceEntity(){
		return findAllDeviceEntityByStatus(REGISTERED);
	}

	public List<DeviceEntity> findAllDisabledDeviceEntity(){
		return findAllDeviceEntityByStatus(DISABLED);
	}

	public List<DeviceEntity> findAllPreregisteredDeviceEntity(){
		return findAllDeviceEntityByStatus(PREREGISTERED);
	}

	public List<DeviceEntity> findAllCoreDeviceEntity(){
		return findAllDeviceEntityByType(Type.CORE);
	}

	public List<DeviceEntity> findAllEdgeDeviceEntity(){
		return findAllDeviceEntityByType(Type.EDGE);
	}

	private List<DeviceEntity> findAllDeviceEntityByStatus(Status status) {
		return deviceRepository.findAll().stream().filter(device -> device.getStatus().equals(status)).toList();
	}

	private List<DeviceEntity> findAllDeviceEntityByType(Type type) {
		return deviceRepository.findAll().stream().filter(device -> device.getType().equals(type)).toList();
	}

	public TagEntity makeTag() {
		return Instancio.of(TagEntity.class)
			.set(field(BaseEntity.class, "id"), new ObjectId())
			.set(field(TagEntity.class, "name"), "tag-test")
			.create();
	}

	public DeviceEntity findDeviceByID(ObjectId id) {
		return deviceRepository.findById(id);
	}
}
