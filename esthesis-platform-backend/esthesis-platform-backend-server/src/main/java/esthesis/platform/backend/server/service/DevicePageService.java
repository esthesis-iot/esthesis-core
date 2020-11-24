package esthesis.platform.backend.server.service;

import esthesis.platform.backend.server.dto.DevicePageDTO;
import esthesis.platform.backend.server.model.DevicePage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@Transactional
public class DevicePageService extends BaseService<DevicePageDTO, DevicePage> {

}
