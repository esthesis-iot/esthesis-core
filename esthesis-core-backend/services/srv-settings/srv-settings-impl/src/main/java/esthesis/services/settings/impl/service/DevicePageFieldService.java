package esthesis.services.settings.impl.service;

import esthesis.service.common.BaseService;
import esthesis.service.settings.entity.DevicePageFieldEntity;
import io.quarkus.panache.common.Sort.Direction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
@ApplicationScoped
public class DevicePageFieldService extends BaseService<DevicePageFieldEntity> {

	public void saveFields(List<DevicePageFieldEntity> fields) {
		getAll().stream().map(DevicePageFieldEntity::getId)
			.filter(
				f -> !fields.stream().map(DevicePageFieldEntity::getId).toList()
					.contains(f)).toList()
			.forEach(objectId -> super.deleteById(objectId.toHexString()));

		fields.forEach(this::save);
	}

	public List<DevicePageFieldEntity> getFields() {
		return getAll("measurement", Direction.Ascending);
	}
}
