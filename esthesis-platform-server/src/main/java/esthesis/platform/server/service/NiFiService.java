package esthesis.platform.server.service;

import esthesis.platform.server.dto.NiFiDTO;
import esthesis.platform.server.mapper.NiFiMapper;
import esthesis.platform.server.model.NiFi;
import esthesis.platform.server.model.QNiFi;
import esthesis.platform.server.nifi.client.dto.NiFiTemplateDTO;
import esthesis.platform.server.nifi.client.services.NiFiClientService;
import esthesis.platform.server.repository.NiFiRepository;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ResourceList;
import io.github.classgraph.ScanResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Transactional
@Validated
@Log
public class NiFiService extends BaseService<NiFiDTO, NiFi> {

  private final NiFiMapper nifiMapper;
  private final NiFiRepository nifiRepository;
  private final NiFiClientService niFiClientService;

  private final String versionPrefix = "nifi/template/esthesis_";
  private final String versionSuffix = ".xml";

  /**
   * Finds the NiFi workflow template version shipped with this version of esthesis.
   */
  public String getLatestWFVersion() {
    String version = getLatestWorkflowTemplateResource();

    // Extract version information from the filename.
    return version.substring(versionPrefix.length(), version.length() - versionSuffix.length());
  }

  private String getLatestWorkflowTemplateResource() {
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

  @CachePut("esthesis.platform.server.activeNiFi")
  public NiFiDTO getActiveNiFi() {
    Optional<NiFi> activeNiFi = this.nifiRepository.findOne(QNiFi.niFi.state.eq(true));
    return activeNiFi.isPresent() ? this.nifiMapper.map(activeNiFi.get()) : null;
  }

  @Override
  @CacheEvict("esthesis.platform.server.activeNiFi")
  public NiFiDTO save(NiFiDTO dto) {
    NiFiDTO activeNiFi = getActiveNiFi();
    if (activeNiFi != null && activeNiFi.getId() != dto.getId() && dto.isState()) {
      activeNiFi.setState(false);
      super.save(activeNiFi);
    }

    NiFiDTO savedDTO = super.save(dto);
    return savedDTO;
  }

  public boolean sync(boolean synced) throws IOException {
    NiFiDTO activeNiFi = getActiveNiFi();
    String latestWorkflowTemplateResource = getLatestWorkflowTemplateResource();
    Optional<NiFiTemplateDTO> template = niFiClientService
      .getTemplate("esthesis_" + getLatestWFVersion());
    NiFiTemplateDTO niFiTemplateDTO = template.isPresent() ? template.get() : null;

    if (niFiTemplateDTO == null) {
      niFiTemplateDTO = niFiClientService
        .uploadTemplate(latestWorkflowTemplateResource);
    }

    if (StringUtils.isEmpty(activeNiFi.getWfVersion())) {
      niFiClientService.instantiateTemplate(niFiTemplateDTO.getId());
    }

    activeNiFi.setWfVersion(getLatestWFVersion());
    activeNiFi.setLastChecked(Instant.now());
    activeNiFi.setSynced(synced);

    save(activeNiFi);

    return true;
  }

  @Override
  @CacheEvict("esthesis.platform.server.activeNiFi")
  public NiFiDTO deleteById(long id) {
    return super.deleteById(id);
  }
}
