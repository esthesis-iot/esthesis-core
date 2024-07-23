package esthesis.services.settings.impl.service;

import static esthesis.common.AppConstants.Security.Category.SETTINGS;
import static esthesis.common.AppConstants.Security.Operation.WRITE;

import esthesis.service.common.BaseService;
import esthesis.service.security.annotation.ErnPermission;
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

	@ErnPermission(category = SETTINGS, operation = WRITE)
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
