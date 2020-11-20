package esthesis.backend.service;

import esthesis.backend.repository.NiFiRepository;
import esthesis.backend.dto.NiFiDTO;
import esthesis.backend.mapper.NiFiMapper;
import esthesis.backend.model.NiFi;
import esthesis.backend.model.QNiFi;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ResourceList;
import io.github.classgraph.ScanResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Transactional
@Validated
@Log
public class NiFiService extends BaseService<NiFiDTO, NiFi> {

  private final NiFiMapper nifiMapper;
  private final NiFiRepository nifiRepository;

  private static final String VERSION_PREFIX = "nifi/template/esthesis_";
  private static final String VERSION_SUFFIX = ".xml";

  /**
   * Finds the NiFi workflow template version shipped with this version of esthesis.
   */
  public String getLatestWFVersion() {
    String version = getLatestWorkflowTemplateResource();

    // Extract version information from the filename.
    return version.substring(VERSION_PREFIX.length(), version.length() - VERSION_SUFFIX.length());
  }

  /**
   * Finds the NiFi workflow template resource shipped with this version of esthesis.
   */
  public String getLatestWorkflowTemplateResource() {
    // Get workflow template from resources.
    try (ScanResult scanResult = new ClassGraph().whitelistPathsNonRecursive("nifi/template")
      .scan()) {
      final ResourceList resources = scanResult.getResourcesWithExtension("xml");
      if (CollectionUtils.isNotEmpty(resources)) {
        return resources.get(resources.size() - 1).getPath();
      }
    }

    return null;
  }

  @CachePut("esthesis.backend.activeNiFi")
  public NiFiDTO getActiveNiFi() {
    Optional<NiFi> activeNiFi = this.nifiRepository.findOne(QNiFi.niFi.state.eq(true));
    return activeNiFi.map(this.nifiMapper::map).orElse(null);
  }

  @Override
  @CacheEvict("esthesis.backend.activeNiFi")
  public NiFiDTO save(NiFiDTO dto) {
    NiFiDTO activeNiFi = getActiveNiFi();
    if (Objects.nonNull(activeNiFi) && !Objects.equals(activeNiFi.getId(), dto.getId()) && dto.isState()) {
      activeNiFi.setState(false);
      super.save(activeNiFi);
    }

    return super.save(dto);
  }

  @Override
  @CacheEvict("esthesis.backend.activeNiFi")
  public NiFiDTO deleteById(long id) {
    return super.deleteById(id);
  }
}
