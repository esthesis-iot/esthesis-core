package esthesis.backend.service;

import esthesis.backend.dto.DevicePageDTO;
import esthesis.backend.model.DevicePage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@Transactional
public class DevicePageService extends BaseService<DevicePageDTO, DevicePage> {

}
