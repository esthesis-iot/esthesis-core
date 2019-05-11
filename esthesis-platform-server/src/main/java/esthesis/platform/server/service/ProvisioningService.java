package esthesis.platform.server.service;

import com.eurodyn.qlack.util.data.optional.ReturnOptional;
import esthesis.platform.server.dto.ProvisioningDTO;
import esthesis.platform.server.mapper.ProvisioningMapper;
import esthesis.platform.server.model.Provisioning;
import esthesis.platform.server.repository.ProvisioningRepository;
import esthesis.platform.server.repository.TagRepository;
import org.apache.commons.collections4.IteratorUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Transactional
@Validated
public class ProvisioningService extends BaseService<ProvisioningDTO, Provisioning> {

  private final ProvisioningMapper provisioningMapper;
  private final ProvisioningRepository provisioningRepository;
  private final TagRepository tagRepository;

  public ProvisioningService(ProvisioningMapper provisioningMapper,
    ProvisioningRepository provisioningRepository,
    TagRepository tagRepository) {
    this.provisioningMapper = provisioningMapper;
    this.provisioningRepository = provisioningRepository;
    this.tagRepository = tagRepository;
  }

  public ProvisioningDTO save(ProvisioningDTO provisioningDTO) {
    // Do not use the generic save from BaseService as we do not want to map the binary content
    // of the provisioning package unnecessarily.
    if (provisioningDTO.getId() != 0) {
      final Provisioning provisioning = ReturnOptional.r(provisioningRepository
        .findById(provisioningDTO.getId()));
      provisioning.setDefaultIP(provisioningDTO.isDefaultIP());
      provisioning.setDescription(provisioningDTO.getDescription());
      provisioning.setName(provisioningDTO.getName());
      provisioning.setPackageVersion(provisioningDTO.getPackageVersion());
      provisioning.setState(provisioningDTO.isState());
      provisioning
        .setTags(
          IteratorUtils.toList(tagRepository.findAllById(provisioningDTO.getTags()).iterator()));
      provisioningRepository.save(provisioning);
      return provisioningMapper.map(provisioning);
    } else {
      return provisioningMapper
        .map(provisioningRepository.save(provisioningMapper.map(provisioningDTO)));
    }
  }
}
