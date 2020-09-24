package esthesis.platform.server.service;

import esthesis.platform.server.dto.DevicePageDTO;
import esthesis.platform.server.model.DevicePage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@Transactional
public class DevicePageService extends BaseService<DevicePageDTO, DevicePage> {

}
