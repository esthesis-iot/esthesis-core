package esthesis.services.device.impl.repository;

import esthesis.core.common.AppConstants.Device;
import esthesis.service.device.entity.DeviceEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Quarkus Panache repository for {@link DeviceEntity}.
 */
@ApplicationScoped
@SuppressWarnings("java:S1192")
public class DeviceRepository implements PanacheMongoRepository<DeviceEntity> {

	public Optional<DeviceEntity> findByHardwareIds(String hardwareId) {
		return find("hardwareId", hardwareId).firstResultOptional();
	}

	public List<DeviceEntity> findByHardwareIds(List<String> hardwareIds) {
		return find("hardwareId in ?1", hardwareIds).list();
	}

	public Optional<DeviceEntity> findByHardwareIdPartial(String hardwareId) {
		return find("hardwareId like ?1", hardwareId).firstResultOptional();
	}

	public List<DeviceEntity> findByHardwareIdPartial(List<String> hardwareIds) {
		return find("hardwareId like ?1", String.join("|", hardwareIds)).list();
	}

	public long countByHardwareId(List<String> hardwareIds) {
		return count("hardwareId in ?1", hardwareIds);
	}

	public long countByHardwareIdPartial(List<String> hardwareIds) {
		return count("hardwareId like ?1", String.join("|", hardwareIds));
	}

	public List<DeviceEntity> findByTagId(List<String> tagIds) {
		return find("tags in ?1", tagIds).list();
	}

	public List<DeviceEntity> findByTagId(String tagId) {
		return find("tags", tagId).list();
	}

	/**
	 * Counts the number of devices in the specific list of tags IDs.
	 *
	 * @param tags The IDs of the tags to search by.
	 */
	public Long countByTag(List<String> tags) {
		return count("tags in ?1", tags);
	}

	public Long countByStatus(Device.Status status) {
		return count("status", status);
	}

	public Long countLastSeenAfter(Instant lastSeen) {
		return count("lastSeen >= ?1", lastSeen);
	}

	public Long countJoinedAfter(Instant joined) {
		return count("registeredOn >= ?1", joined);
	}

}
