package esthesis.platform.server.service;

import com.eurodyn.qlack.common.util.KeyValue;
import com.google.common.collect.Sets;
import esthesis.platform.server.config.AppConstants;
import esthesis.platform.server.dto.ApplicationDTO;
import esthesis.platform.server.model.Application;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Set;

@Service
@Transactional
@Validated
public class ApplicationService extends BaseService<ApplicationDTO, Application> {

  public ApplicationService() {
  }

  public Set<KeyValue> getStatuses() {
    return Sets.newHashSet(
        KeyValue.builder().key("Disabled").value(AppConstants.Application.STATUS_DISABLED).build(),
        KeyValue.builder().key("Active").value(AppConstants.Application.STATUS_ACTIVE).build(),
        KeyValue.builder().key("Inactive").value(AppConstants.Application.STATUS_INACTIVE).build());
  }
}

