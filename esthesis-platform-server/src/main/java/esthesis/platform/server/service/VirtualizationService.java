package esthesis.platform.server.service;

import esthesis.platform.server.dto.VirtualizationDTO;
import esthesis.platform.server.model.Virtualization;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Transactional
@Validated
@NoArgsConstructor
public class VirtualizationService extends BaseService<VirtualizationDTO, Virtualization> {

}
