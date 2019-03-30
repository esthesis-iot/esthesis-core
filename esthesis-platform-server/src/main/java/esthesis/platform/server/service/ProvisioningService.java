package esthesis.platform.server.service;

import esthesis.platform.server.dto.ProvisioningDTO;
import esthesis.platform.server.mapper.ProvisioningMapper;
import esthesis.platform.server.model.Provisioning;
import esthesis.platform.server.repository.ProvisioningRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@Transactional
@Validated
public class ProvisioningService extends BaseService<ProvisioningDTO, Provisioning> {

  private final ProvisioningMapper provisioningMapper;
  private final ProvisioningRepository provisioningRepository;

  public ProvisioningService(ProvisioningMapper provisioningMapper, ProvisioningRepository provisioningRepository) {
    this.provisioningMapper = provisioningMapper;
    this.provisioningRepository = provisioningRepository;
  }

  public ProvisioningDTO save(ProvisioningDTO provisioningPackageDTO, MultipartFile file) throws IOException {
    provisioningPackageDTO.setFileSize(file.getSize());

    return provisioningMapper.map(provisioningRepository.save(provisioningMapper.map(provisioningPackageDTO)));
  }

}
