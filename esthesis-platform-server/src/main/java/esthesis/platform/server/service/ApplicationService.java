package esthesis.platform.server.service;

import esthesis.platform.server.dto.ApplicationDTO;
import esthesis.platform.server.model.Application;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Transactional
@Validated
public class ApplicationService extends BaseService<ApplicationDTO, Application> {

}

