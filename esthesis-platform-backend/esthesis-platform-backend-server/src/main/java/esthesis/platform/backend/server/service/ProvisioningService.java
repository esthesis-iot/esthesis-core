package esthesis.platform.backend.server.service;

import com.querydsl.core.types.Predicate;
import esthesis.platform.backend.common.device.dto.DeviceDTO;
import esthesis.platform.backend.server.config.AppProperties;
import esthesis.platform.backend.server.dto.ProvisioningDTO;
import esthesis.platform.backend.server.mapper.ProvisioningMapper;
import esthesis.platform.backend.server.model.Provisioning;
import esthesis.platform.backend.server.repository.ProvisioningContentStore;
import esthesis.platform.backend.server.repository.ProvisioningRepository;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Optional;
import java.util.logging.Level;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

@Log
@Service
@Validated
@Transactional
@RequiredArgsConstructor
public class ProvisioningService extends BaseService<ProvisioningDTO, Provisioning> {

  private final ProvisioningMapper provisioningMapper;
  private final ProvisioningRepository provisioningRepository;
  private final ProvisioningContentStore provisioningContentStore;
  private final AppProperties appProperties;

  @Override
  public Page<ProvisioningDTO> findAll(Predicate predicate, Pageable pageable) {
    return provisioningMapper.map(provisioningRepository.findAll(predicate, pageable));
  }

  public InputStream download(long provisioningId)
  throws IOException {
    final Provisioning provisioning = findEntityById(provisioningId);
      return provisioningContentStore.getContent(provisioning);
  }

  public long save(ProvisioningDTO provisioningDTO, MultipartFile file) throws IOException {
    return super.save(provisioningDTO, file.getInputStream()).getId();
  }

  public Optional<ProvisioningDTO> matchByTag(DeviceDTO deviceDTO) {
    Optional<ProvisioningDTO> provisioningDTO;

    if (CollectionUtils.isEmpty(deviceDTO.getTags())) {
      provisioningDTO = findAll().stream()
        .filter(ProvisioningDTO::isState)
        .filter(o -> o.getTags().isEmpty())
        .max(Comparator.comparing(ProvisioningDTO::getPackageVersion));
    } else {
      provisioningDTO = findAll().stream()
        .filter(ProvisioningDTO::isState)
        .filter(o ->
          CollectionUtils.intersection(deviceDTO.getTags(), o.getTags()).size() ==
            deviceDTO.getTags().size())
        .max(Comparator.comparing(ProvisioningDTO::getPackageVersion));
    }

    return provisioningDTO;
  }

  @Override
  public ProvisioningDTO deleteById(long id) {
    String contentId = findEntityById(id).getContentId();
    final Path encryptedFile = Paths
      .get(appProperties.getFsProvisioningRoot(), contentId + ".encrypted");
    try {
      Files.deleteIfExists(encryptedFile);
    } catch (IOException e) {
      log.log(Level.SEVERE, "Could not delete encrypted provisioning file: {0}.",
        encryptedFile.toAbsolutePath().toString());
    }
    return super.deleteById(id);
  }
}
