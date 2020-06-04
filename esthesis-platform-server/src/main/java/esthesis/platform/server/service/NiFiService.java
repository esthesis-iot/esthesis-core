package esthesis.platform.server.service;

import com.eurodyn.qlack.util.data.optional.ReturnOptional;
import esthesis.platform.server.dto.NiFiDTO;
import esthesis.platform.server.mapper.NiFiMapper;
import esthesis.platform.server.model.NiFi;
import esthesis.platform.server.model.QNiFi;
import esthesis.platform.server.repository.NiFiRepository;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ResourceList;
import io.github.classgraph.ScanResult;
import lombok.extern.java.Log;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Transactional
@Validated
@Log
public class NiFiService extends BaseService<NiFiDTO, NiFi> {

  private final NiFiMapper nifiMapper;
  private final NiFiRepository nifiRepository;

  public NiFiService(NiFiMapper nifiMapper, NiFiRepository nifiRepository) {
    this.nifiMapper = nifiMapper;
    this.nifiRepository = nifiRepository;
  }

  /**
   * Finds the NiFi workflow template version shipped with this version of esthesis.
   */
  public String getLatestWFVersion() {
    String version = null;

    // Get workflow template from resources.
    try (ScanResult scanResult = new ClassGraph().whitelistPathsNonRecursive("nifi/template")
      .scan()) {
      final ResourceList resources = scanResult.getResourcesWithExtension("xml");
      if (CollectionUtils.isNotEmpty(resources)) {
        version = resources.get(resources.size() - 1).getPath();
      }
    }

    // Extract version information from the filename.
    String versionPrefix = "nifi/template/esthesis_";
    String versionSuffix = ".xml";
    return version.substring(versionPrefix.length(), version.length() - versionSuffix.length());
  }

  @Cacheable("esthesis.platform.server.activeNiFi")
  public NiFiDTO getActiveNiFi() {
    NiFi activeNiFi = ReturnOptional.r(this.nifiRepository.findOne(QNiFi.niFi.state.eq(true)));
    return this.nifiMapper.map(activeNiFi);
  }

  @Override
  @CacheEvict("esthesis.platform.server.activeNiFi")
  public NiFiDTO save(NiFiDTO dto) {
    if (this.nifiRepository.count() > 0 && dto.isState()) {
      NiFiDTO activeNiFi = getActiveNiFi();
      activeNiFi.setState(false);
      super.save(activeNiFi);
    }
    return super.save(dto);
  }
}
