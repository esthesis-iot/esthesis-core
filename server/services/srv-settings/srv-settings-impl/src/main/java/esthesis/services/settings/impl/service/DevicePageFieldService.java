package esthesis.services.settings.impl.service;

import esthesis.service.common.BaseService;
import esthesis.service.settings.dto.DevicePageField;
import io.quarkus.panache.common.Sort.Direction;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class DevicePageFieldService extends BaseService<DevicePageField> {

  public void saveFields(List<DevicePageField> fields) {
    getAll().stream().map(DevicePageField::getId)
        .filter(
            f -> !fields.stream().map(DevicePageField::getId).toList()
                .contains(f)).toList()
        .forEach(this::deleteById);

    fields.forEach(this::save);
  }

  public List<DevicePageField> getFields() {
    return getAll("measurement", Direction.Ascending);
  }
}
